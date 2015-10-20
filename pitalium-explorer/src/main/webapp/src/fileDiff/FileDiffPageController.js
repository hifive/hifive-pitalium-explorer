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
		'_expectedImageListController': hifive.pitalium.explorer.controller.ImageListController,
		'_actualImageListController': hifive.pitalium.explorer.controller.ImageListController,

		'__meta': {
			'_dividedboxController': {
				'rootElement': '.dividedbox'
			},
			'_testResultDiffController': {
				'rootElement': '#imageDiffContainer'
			},
			'_fileUploadController': {
				'rootElement': '#fileUploadContainer'
			},
			'_expectedImageListController': {
				'rootElement': '#imageListContainer > .image-list.expected'
			},
			'_actualImageListController': {
				'rootElement': '#imageListContainer > .image-list.actual'
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

		'__ready': function() {
			// ターゲット選択ドロップダウンを非表示
			this.$find('.image-selector-row').hide();

			// Expectedモードを非表示
			this.$find('#imageDiffContainer #expected-mode').hide();

			this._fileUploadController.setTarget(this.$find('#imageDiffContainer'));
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

		'_showResult': function() {
			// 両方のIDが登録されていない場合Expectedモードで表示
			if (!this._validateScreenshot()) {
				var id = this._screenshot.id !== null ? this._screenshot.id
						: this._screenshot.expectedScreenshotId;
				var targets = this._screenshot.targets;
				this._testResultDiffController.showResult({
					'id': id,
					'targets': targets
				});

				return;
			}

			this._testResultDiffController.showResult(this._screenshot);
		},

		'{rootElement} uploadFile': function(context) {
			var args = context.evArg;
			if (args.mode == 'expected') {
				this.setExpectedScreenshotId(args.screenshotId);
				this._expectedImageListController.addTemporaryFile(args);
			} else {
				this.setActualScreenshotId(args.screenshotId);
				this._actualImageListController.addTemporaryFile(args);
			}

			this._showResult();
		},

		'{rootElement} screenshotSelect': function(context) {
			var args = context.evArg;
			if (args.mode == 'expected') {
				this.setExpectedScreenshotId(args.screenshotId);
			} else {
				this.setActualScreenshotId(args.screenshotId);
			}

			this._showResult();
		},

		'{rootElement} selectExecution': function(context) {
			var args = context.evArg;
			if (args.mode == 'expected') {
				this._actualImageListController.disableSelectExecution();
			} else {
				this._expectedImageListController.disableSelectExecution();
			}
		},

		'{window} [resize]': function() {
			this._dividedboxController.refresh();
			this._fileUploadController.resetPosition();
		},

		'{window} dragenter': function(context, $el) {
			this._fileUploadController.dragStart(context, $el);
		},

		'{window} dragleave': function() {
			this._fileUploadController.dragLeaved();
		}

	};

	h5.core.expose(fileDiffPageController);
})(jQuery);
