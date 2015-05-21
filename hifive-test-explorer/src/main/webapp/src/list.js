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
		 * Gets a list of test execution.
		 * 
		 * @memberOf hifive.test.explorer.logic.TestResultListLogic
		 * @returns {JqXHRWrapper}
		 */
		getTestExecutionList: function() {
			return h5.ajax({
				type: 'get',
				dataType: 'json',
				url: hifive.test.explorer.utils.formatUrl('api/listTestExecution')
			});
		},

		/**
		 * Gets a list of test execution which is narrowed down by parameters.
		 * 
		 * @memberOf hifive.test.explorer.logic.TestResultListLogic
		 * @param {Object} params search parameters
		 * @return {JqXHRWrapper}
		 */
		getTestExecutionListWithParams: function(params) {
			var data = {};
			this._copyObjectByKey(params, data, ['criteria']);

			return h5.ajax({
				type: 'get',
				dataType: 'json',
				url: 'api/listTestExecution/search',
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
					testExecutionId: testExecutionId
				}
			});
		},

		/**
		 * Copy values which are specified by "keys" parameter from one object to other.
		 * 
		 * @memberOf hifive.test.explorer.logic.TestResultListLogic
		 * @param {Object} from The object copy from.
		 * @param {Object} to The object copy to.
		 * @param {Array} keys
		 */
		_copyObjectByKey: function(from, to, keys) {
			for ( var index in keys) {
				var key = keys[index];
				if (from.hasOwnProperty(key)) {
					to[key] = from[key];
				}
			}
		}
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
			var indicator = this.indicator({
				message: 'Loading...',
				target: document
			}).show();

			// Load list of test execution
			this._testResultListLogic.getTestExecutionList().done(
					this.own(function(testExecutionList) {
						// Update views
						this.view.update('#testExecutionList', 'testExecutionListTemplate', {
							testExecutionsPage: testExecutionList
						});
					})).always(function() {
				indicator.hide();
			});
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

			// Collect search parameters
			var params = {};
			$el.find('input').each(function(index) {
				var $elem = $(this);
				params[$elem.attr('name')] = $elem.val();
			});

			// Show indicator
			var indicator = this.indicator({
				message: 'Loading...',
				target: document
			}).show();

			// Reset views
			this.$find('#testExecutionList').empty();

			// Search test results
			this._testResultListLogic.getTestExecutionListWithParams(params).done(
					this.own(function(testExecutionList) {
						this.view.update('#testExecutionList', 'testExecutionList', {
							testExecutions: testExecutionList
						});
					})).always(function() {
				indicator.hide();
			});
		}
	};
	h5.core.expose(testResultListController);
})(jQuery);
$(function() {
	h5.core.controller('body>div.container', hifive.test.explorer.controller.TestResultListController);
});