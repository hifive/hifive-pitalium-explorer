/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
	var utils = hifive.pitalium.explorer.utils;

	/**
	 * This class is a &quot;Logic&quot; for the list page of test results.
	 * 
	 * @class
	 * @memberOf hifive.pitalium.explorer.logic
	 * @name TestResultListLogic
	 */
	var testResultListLogic = {
		/**
		 * @memberOf hifive.pitalium.explorer.logic.TestResultListLogic
		 */
		__name: 'hifive.pitalium.explorer.logic.TestResultListLogic',

		/**
		 * The search keyword for test method.
		 * 
		 * @type String
		 * @memberOf hifive.pitalium.explorer.logic.TestResultListLogic
		 */
		searchTestMethod: "",

		/**
		 * The search keyword for test screen.
		 * 
		 * @type String
		 * @memberOf hifive.pitalium.explorer.logic.TestResultListLogic
		 */
		searchTestScreen: "",

		/**
		 * Gets a list of test execution.
		 * 
		 * @memberOf hifive.pitalium.explorer.logic.TestResultListLogic
		 * @param {Number} page desired
		 * @param {number} pageSize new page size
		 * @returns {JqXHRWrapper}
		 */
		getTestExecutionList: function(page, pageSize) {
			var data = {
				'page': page,
				'limit': pageSize,
				'searchTestMethod': this.searchTestMethod,
				'searchTestScreen': this.searchTestScreen,
				resultDirectoryKey: utils.getParameters().resultDirectoryKey
			};

			return h5.ajax({
				type: 'get',
				dataType: 'json',
				url: hifive.pitalium.explorer.utils.formatUrl('executions/list'),
				data: data
			});
		},

		/**
		 * Gets a list of screenshots.
		 * 
		 * @memberOf hifive.pitalium.explorer.logic.TestResultListLogic
		 * @param {string} testExecutionId The time the test was run.
		 * @returns {JqXHRWrapper}
		 */
		getScreenshotList: function(testExecutionId) {
			return h5.ajax({
				type: 'get',
				dataType: 'json',
				url: 'screenshots/search',
				data: {
					testExecutionId: testExecutionId,
					searchTestMethod: this.searchTestMethod,
					searchTestScreen: this.searchTestScreen
				}
			});
		}
	};
	h5.core.expose(testResultListLogic);
})(jQuery);