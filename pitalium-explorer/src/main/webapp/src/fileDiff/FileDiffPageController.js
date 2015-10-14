/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */

(function($) {

	/**
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name FileDiffPageController
	 */
	var fileDiffPageController = {

		/**
		 * @memberOf hifive.pitalium.explorer.controller.FileDiffPageController
		 */
		'__name': 'hifive.pitalium.explorer.controller.FileDiffPageController',

		'_dividedboxController': h5.ui.components.DividedBox.DividedBox,

		'_testResultDiffController': hifive.pitalium.explorer.controller.TestResultDiffController,

		'_fileUploadController': hifive.pitalium.explorer.controller.FileUploadController,

		'__meta': {
			'_dividedboxController': {
				'rootElement': '.dividedbox'
			},
			'_testResultDiffController': {
				'rootElement': '#imageDiffContainer'
			},
			'_fileUploadController': {
				'rootElement': '#fileUploadContainer'
			}
		},

		'_screenshot': {
			'id': null,
			'expectedScreenshotId': null,
			'targets': [{
				'targetId': 0,
				'area': {
					'selectorType': '',
					'selectorValue': '',
					'selectorIndex': 0
				}
			}]
		},

		'__construct': function() {
			this.log.debug('FileDiffPageController construct');
		},

		'__init': function() {
			this.log.debug('FileDiffPageController init');
		},

		'__postInit': function() {
			this.log.debug('FileDiffPageController postInit');
		},

		'__ready': function() {
			this.log.debug('FileDiffPageController ready');
		},

		'setExpectedScreenshotId': function(screenshotId) {
			this._screenshot.expectedScreenshotId = screenshotId;
		},

		'setActualScreenshotId': function(screenshotId) {
			this._screenshot.id = screenshotId;
		},

		'_validateScreenshot': function() {
			return this._screenshot.id !== null && this._screenshot.expectedScreenshotId !== null;
		},

		'{rootElement} ptlFileUploaded': function(context) {
			var args = context.evArg;
			if (args.mode == 'expected') {
				this.setExpectedScreenshotId(args.screenshotId);
			} else {
				this.setActualScreenshotId(args.screenshotId);
			}

			if (!this._validateScreenshot()) {
				return;
			}

			this._testResultDiffController.showResult(this._screenshot);
		}

	};

	h5.core.expose(fileDiffPageController);
})(jQuery);
