/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
	/**
	 * This class is a &qout;logic&quot; for the test result comparison page.
	 * 
	 * @class
	 * @memberOf hifive.pitalium.explorer.logic
	 * @name TestResultDiffLogic
	 */
	var testResultDiffLogic = {
		/**
		 * @memberOf hifive.pitalium.explorer.logic.TestResultDiffLogic
		 */
		__name: 'hifive.pitalium.explorer.logic.TestResultDiffLogic',

		/**
		 * Get details of the screenshot.
		 * 
		 * @memberOf hifive.pitalium.explorer.logic.TestResultDiffLogic
		 * @param {string} id the id of the screenshot
		 * @returns {JqXHRWrapper}
		 */
		getScreenshot: function(id) {
			return h5.ajax({
				type: 'get',
				url: 'screenshot',
				data: {
					screenshotId: id
				}
			});
		},

		/**
		 * Get screenshots of the current execution and environment.
		 * 
		 * @memberOf hifive.pitalium.explorer.logic.TestResultDiffLogic
		 * @param {string} id the id of the screenshot
		 * @returns {JqXHRWrapper}
		 */
		listScreenshot: function(executionId, environmentId) {
			var dfd = this.deferred();

			h5.ajax('screenshots/list', {
				data: {
					testExecutionId: executionId,
					testEnvironmentId: environmentId
				},
				type: 'GET',
				dataType: 'json'
			}).done(this.own(function(response) {
				var screenshots = response.content;
				var map = this._convertToTreeMap(screenshots);
				dfd.resolve(map);
			}));
			return dfd.promise();
		},

		_convertToTreeMap: function(screenshotArray) {
			var retMap = {};
			for (var i = 0, len = screenshotArray.length; i < len; i++) {
				var s = screenshotArray[i];
				var testClass = s.testClass;
				var testClassObj = retMap[testClass];
				if (!testClassObj) {
					testClassObj = {};
					retMap[testClass] = testClassObj;
				}

				var testMethod = s.testMethod;
				var screenshotsOfMethod = testClassObj[testMethod];
				if (!screenshotsOfMethod) {
					screenshotsOfMethod = [];
					testClassObj[testMethod] = screenshotsOfMethod;
				}

				screenshotsOfMethod.push(s);
			}
			return retMap;
		},

		listTestExecutionsWithEnvironment: function() {
			return h5.ajax({
				type: 'get',
				url: 'executions/environments/list'
			});
		},

		getComparisonResult: function(screenshot) {
			return h5.ajax('comparisonResult', {
				data: {
					sourceScreenshotId: screenshot.actual.screenshotId,
					targetScreenshotId: screenshot.expected.screenshotId,
					sourceTargetId: screenshot.actual.targetId,
					targetTargetId: screenshot.expected.targetId
				},
				type: 'GET',
				dataType: 'json'
			});
		},

		updateTargetResult: function(changes) {
			return h5.ajax('targets/update', {
				data: JSON.stringify([changes]),
				type: 'POST',
				contentType: 'application/json',
				dataType: 'json'
			});
		}
	};

	h5.core.expose(testResultDiffLogic);
})(jQuery);