/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
	/**
	 * This class is a controller for the list page of test results.
	 * 
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name ResultListPageController
	 */
	var resultListPageController = {
		/**
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 */
		__name: 'hifive.pitalium.explorer.controller.ResultListPageController',

		/**
		 * The &quot;logic&quot; class
		 * 
		 * @type Logic
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 */
		_testResultListLogic: hifive.pitalium.explorer.logic.TestResultListLogic,

		/**
		 * The number of items to show in one page.
		 * 
		 * @type Number
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 */
		_pageSize: 20,

		/**
		 * The 0-based index of the item at the top of the current page.
		 * 
		 * @type Number
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 */
		_pageStart: 0,

		/**
		 * Called after the controller has been initialized.<br>
		 * Load list of test execution time asynchronously and update views.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
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
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'.explorer-collapsable show.bs.collapse': function(context, $el) {
			var $panelBody = $el.find('.panel-body');

			// Check the loaded flag and do nothing if exists.
			if ($panelBody.hasClass('hifive.pitalium.explorer-load')) {
				return;
			}

			var testExecutionId = $el.data('testExecutionId');

			$panelBody.addClass('hifive.pitalium.explorer-load');

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
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'.explorer-test-result click': function(context, $el) {
			var id = $el.data('screenshotId');
			var url = hifive.pitalium.explorer.utils.formatUrl('diff.html', {
				id: id
			});

			window.open(url, '_diff');
		},

		/**
		 * Called when the search form has been opened.<br>
		 * Update button label.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
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
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
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
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'#searchTest submit': function(context, $el) {
			// Stop submit
			context.event.preventDefault();

			this.collectSearchParameters($el);
			if (window.location.hash) {
				window.location.hash = '';
			} else {
				this.updatePage();
			}
		},

		/**
		 * Called when the page size select value has been changed. Updates view.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 * @param {Object} context the event context
		 * @param {jQuery} $el the event target element
		 */
		'#select-page-size change': function(context, $el) {
			this._pageSize = $el.val();
			this.updatePage();
		},

		/**
		 * Called when the page link has been clicked. Update view.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 */
		onHashChange: function() {
			var pageStart = Math.max(0, parseInt(window.location.hash.substr(1), 10));
			if (isNaN(pageStart)) {
				pageStart = 0;
			}
			this._pageStart = pageStart;

			this.updatePage();
		},

		/**
		 * Collect search keyword parameters and save them to logic.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 * @param {jQuery} $el the search element
		 */
		collectSearchParameters: function($el) {
			if (typeof ($el) == 'undefined') {
				$el = this.$find('#searchTest');
			}

			var params = {};
			$el.find('input').each(function(index) {
				var $elem = $(this);
				params[$elem.attr('name')] = $elem.val();
			});

			this._testResultListLogic.searchTestMethod = params.searchTestMethod;
			this._testResultListLogic.searchTestScreen = params.searchTestScreen;
		},

		/**
		 * Set pageSize and pageStart, and update view.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 */
		updatePage: function() {
			var page = 1 + Math.floor(this._pageStart / this._pageSize);
			this.loadTestExecutionList(page);
		},

		/**
		 * Load test execution list from server and update view.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.ResultListPageController
		 * @param {number} page desired page number
		 */
		loadTestExecutionList: function(page) {
			var indicator = this.indicator({
				message: 'Loading...',
				target: document
			}).show();

			// Load list of test execution
			this._testResultListLogic.getTestExecutionList(page, this._pageSize).done(
					this.own(function(response) {
						// Update views
						this.view.update('#testExecutionList', 'testExecutionListTemplate', {
							testExecutionsPage: response
						});
					})).always(function() {
				indicator.hide();
			});
		}
	};
	h5.core.expose(resultListPageController);
})(jQuery);
