/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
(function() {
	'use strict';

	/**
	 * This class is a &quot;Logic&quot; for the list page of test results.
	 * 
	 * @class
	 * @name TestResultListLogic
	 */

	/**
	 * @lends hifive.pitalium.explorer.logic.TestResultListLogic#
	 */
	var PageLogic = {
		/**
		 * @ignore
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
				'searchTestScreen': this.searchTestScreen
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
			var dfd = this.deferred();
			h5.ajax({
				type: 'get',
				dataType: 'json',
				url: 'screenshots/search',
				data: {
					testExecutionId: testExecutionId,
					searchTestMethod: this.searchTestMethod,
					searchTestScreen: this.searchTestScreen
				}
			}).done(function(screenshotList) {
				var testClassResult = {};
				for (var i = 0, len = screenshotList.length; i < len; i++) {
					var screenshot = screenshotList[i];
					var testClass = screenshot.testClass;
					var classResult = testClassResult[testClass];
					if (!classResult) {
						classResult = {
							list: [],
							result: null
						};
						testClassResult[testClass] = classResult;
					}
					if (screenshot.comparisonResult && classResult.result == null) {
						classResult.result = true;
					}
					if (screenshot.comparisonResult === false) {
						classResult.result = false;
					}

					classResult.list.push(screenshot);
				}
				dfd.resolve(testClassResult);
			});
			return dfd.promise();
		},

		/**
		 * @param {String} path
		 * @param {Boolean} [refresh = true]
		 */
		fetchScreenshotList: function(path, refresh) {
			if (refresh !== false) {
				refresh = true;
			}

			return h5.ajax({
				url: './_screenshots/list.json',
				data: {
					path: path,
					refresh: refresh + ''
				}
			});
		},

		/**
		 * @param {String} targets
		 * @param {String} expected
		 * @returns {JQueryPromise}
		 */
		compareScreenshots: function(targets, expected) {
			return h5.ajax({
				type: 'POST',
				url: '_screenshots/compare.json',
				data: {
					targets: targets,
					expected: expected
				}
			});
		},

		/**
		 * @param {String} path
		 * @param {String} id
		 * @returns {JQueryPromise}
		 */
		deleteScreenshot: function(path, id) {
			return h5.ajax({
				url: '_screenshots/delete',
				data: {
					path: path,
					resultListId: id
				}
			});
		},

		getResultDirectoryKeys: function() {
			return h5.ajax({
				type: 'get',
				dataType: 'json',
				url: 'directoryKeys/list'
			});
		}
	};

	h5.core.expose(PageLogic);

})();