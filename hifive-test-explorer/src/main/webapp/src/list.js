/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
	/**
	 * This class is a &quot;Logic&quot; for the list page of test results.
	 * 
	 * @class
	 * @memberOf hifive.test.explorer.logic
	 * @name TestResultListLogic
	 */
	var testResultListLogic = {
		/**
		 * @memberOf hifive.test.explorer.logic.TestResultListLogic
		 */
		__name: 'hifive.test.explorer.logic.TestResultListLogic',

		/**
		 * The number of items to show in one page.
		 *
		 * @type Number
		 * @memberOf hifive.test.explorer.logic.TestResultListLogic
		 */
		pageSize: 20,

		/**
		 * The 0-based index of the item at the top of the current page.
		 *
		 * @type Number
		 * @memberOf hifive.test.explorer.logic.TestResultListLogic
		 */
		pageStart: 0,

		/**
		 * The search keyword for test method.
		 *
		 * @type String
		 * @memberOf hifive.test.explorer.logic.TestResultListLogic
		 */
		searchTestMethod: "",

		/**
		 * The search keyword for test screen.
		 *
		 * @type String
		 * @memberOf hifive.test.explorer.logic.TestResultListLogic
		 */
		searchTestScreen: "",

		/**
		 * Gets a list of test execution.
		 * 
		 * @memberOf hifive.test.explorer.logic.TestResultListLogic
		 * @param {Number} page desired
		 * @returns {JqXHRWrapper}
		 */
		getTestExecutionList: function(page) {
			var data = {
				'page': page,
				'limit': this.pageSize,
				'searchTestMethod': this.searchTestMethod,
				'searchTestScreen': this.searchTestScreen,
			};

			return h5.ajax({
				type: 'get',
				dataType: 'json',
				url: hifive.test.explorer.utils.formatUrl('api/listTestExecution'),
				data: data
			});
		},

		/**
		 * Gets a list of screenshots.
		 * 
		 * @memberOf hifive.test.explorer.logic.TestResultListLogic
		 * @param {string} testExecutionId The time the test was run.
		 * @returns {JqXHRWrapper}
		 */
		getScreenshotList: function(testExecutionId) {
			return h5.ajax({
				type: 'get',
				dataType: 'json',
				url: 'api/listScreenshot',
				data: {
					testExecutionId: testExecutionId,
					searchTestMethod: this.searchTestMethod,
					searchTestScreen: this.searchTestScreen,
				}
			});
		},
	};
	h5.core.expose(testResultListLogic);
})(jQuery);
(function($) {
	/**
	 * This class is a controller for the list page of test results.
	 * 
	 * @class
	 * @memberOf hifive.test.explorer.controller
	 * @name TestResultListController
	 */
	var testResultListController = {
		/**
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 */
		__name: 'hifive.test.explorer.controller.TestResultListController',

		/**
		 * The &quot;logic&quot; class
		 * 
		 * @type Logic
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 */
		_testResultListLogic: hifive.test.explorer.logic.TestResultListLogic,

		/**
		 * Called after the controller has been initialized.<br>
		 * Load list of test execution time asynchronously and update views.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 */
		__ready: function() {
			this.collectSearchParameters();
			this.onHashChange();
			$(window).on('hashchange', this.own(this.onHashChange));
		},

		/**
		 * Called when a label of test execution has been clicked.<br>
		 * Load list of test results of selected item asynchronously, and update views.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'.explorer-collapsable show.bs.collapse': function(context, $el) {
			var $panelBody = $el.find('.panel-body');

			// Check the loaded flag and do nothing if exists.
			if ($panelBody.hasClass('hifive.test.explorer-load'))
				return;

			var testExecutionId = $el.data('testExecutionId');

			$panelBody.addClass('hifive.test.explorer-load');

			// Show indicator
			var indicator = this.indicator({
				message: 'Loading...',
				target: document
			}).show();

			this._testResultListLogic.getScreenshotList(testExecutionId).done(
					this.own(function(screenshotList) {
						// Update views
						this.view.update($panelBody, 'screenshotListTemplate', {
							screenshots: screenshotList
						});
					})).always(function() {
				indicator.hide();
			});
		},

		/**
		 * Called when a test result has been clicked.<br>
		 * Go to a new page which shows the difference images of the selected test result.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'.explorer-test-result click': function(context, $el) {
			var id = $el.data('screenshotId');
			var url = hifive.test.explorer.utils.formatUrl('diff.html', {
				id: id
			});

			location.href = url;
		},

		/**
		 * Called when the search form has been opened.<br>
		 * Update button label.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'#searchPanel show.bs.collapse': function(context, $el) {
			this.$find('#toggleSearchPanel').text('Close search');
		},

		/**
		 * Called when the search form has been closed.<br>
		 * Update button label.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'#searchPanel hide.bs.collapse': function(context, $el) {
			this.$find('#toggleSearchPanel').text('Open search');
		},

		/**
		 * Called when the search form has been submitted.<br>
		 * Collect input parameters, search test results asynchronously, and update views.
		 * 
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'#searchTest submit': function(context, $el) {
			// Stop submit
			context.event.preventDefault();

			this.collectSearchParameters($el);
			window.location.hash = '';
		},

		/**
		 * Called when the page size select value has been changed. Updates view.
		 *
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'#select-page-size change': function(context, $el) {
			var pageSize = $el.val();
			var pageStart = this._testResultListLogic.pageStart;
			this.updatePageSize(pageSize, pageStart);
		},

		/**
		 * Called when the page link has been clicked. Update view.
		 *
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 */
		onHashChange: function(){
			var pageSize = $("#select-page-size").val();
			var pageStart = Math.max(0, parseInt(window.location.hash.substr(1)));
			if (isNaN(pageStart)) { pageStart = 0; }

			this.updatePageSize(pageSize, pageStart);
		},

		/**
		 * Collect search keyword parameters and save them to logic.
		 *
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 * @param {jQuery} $el the search element
		 */
		collectSearchParameters: function($el) {
			if (typeof($el) == 'undefined') {
				$el = this.$find('#searchTest');
			}

			var params = {};
			$el.find('input').each(function(index) {
				var $elem = $(this);
				params[$elem.attr('name')] = $elem.val();
			});

			this._testResultListLogic.searchTestMethod = params['searchTestMethod'];
			this._testResultListLogic.searchTestScreen = params['searchTestScreen'];
		},

		/**
		 * Set pageSize and pageStart, and update view.
		 *
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 * @param {number} pageSize new page size
		 * @param {number} pageStart new page start
		 */
		updatePageSize: function(pageSize, pageStart) {
			// update pagination parameters
			this._testResultListLogic.pageStart = pageStart;
			this._testResultListLogic.pageSize = pageSize;
			var page = 1 + Math.floor(pageStart / pageSize);
			this.loadTestExecutionList(page);
		},

		/**
		 * Load test execution list from server and update view.
		 *
		 * @memberOf hifive.test.explorer.controller.TestResultListController
		 * @param {number} page desired page number
		 */
		loadTestExecutionList: function(page) {
			var indicator = this.indicator({
				message: 'Loading...',
				target: document
			}).show();

			// Load list of test execution
			this._testResultListLogic.getTestExecutionList(page).done(this.own(function(response) {
				// Update views
				this.view.update('#testExecutionList', 'testExecutionListTemplate', {
					testExecutionsPage: response
				});
			})).always(function() {
				indicator.hide();
			});
		},
	};
	h5.core.expose(testResultListController);
})(jQuery);
$(function() {
	h5.core.controller('body>div.container', hifive.test.explorer.controller.TestResultListController);
});
