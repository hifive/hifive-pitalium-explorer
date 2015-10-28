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
			'expected': null,
			'actual': null
		},

		'__ready': function() {
			// ターゲット選択ドロップダウンを非表示
			this.$find('.image-selector-row').hide();

			// Expectedモードを非表示
			this.$find('#imageDiffContainer #expected-mode').hide();

			this._fileUploadController.setTarget(this.$find('#imageDiffContainer'));
		},

		'setExpected': function(screenshot) {
			this._screenshot.expected = {
				'screenshotId': screenshot.screenshotId,
				'targetId': screenshot.targetId
			};
		},

		'setActual': function(screenshot) {
			this._screenshot.actual = {
				'screenshotId': screenshot.screenshotId,
				'targetId': screenshot.targetId
			};
		},

		'setExpectedScreenshotId': function(id) {
			this.setExpected({
				'screenshotId': id,
				'targetId': 0
			});
		},

		'setActualScreenshotId': function(id) {
			this.setActual({
				'screenshotId': id,
				'targetId': 0
			});
		},

		'_validateScreenshot': function() {
			return this._screenshot.expected && this._screenshot.actual;
		},

		'_showResult': function() {
			// Show as expected mode when both expected id and actual id are not registered.
			if (!this._validateScreenshot()) {
				var data = this._screenshot.expected ? this._screenshot.expected
						: this._screenshot.actual;
				this._testResultDiffController.showResult({
					'actual': data
				});

				return;
			}

			this._testResultDiffController.showResult(this._screenshot);
		},

		/**
		 * Handle events of file upload sent by FileUploadController.
		 *
		 * @param {HifiveEventContext} context
		 * @method
		 * @memberOf hifive.pitalium.explorer.controller.FileDiffPageController
		 */
		'{rootElement} uploadFile': function(context) {
			var arg = context.evArg;
			var findFirstScreenshotId = function(file) {
				if (file.isFile) {
					return file.screenshotId;
				}

				var files;
				if (jQuery.isArray(file)) {
					files = file;
				} else {
					files = file.children;
				}

				for (var i = 0; i < files.length; i++) {
					var id = findFirstScreenshotId(files[i]);
					if (id !== null) {
						return id;
					}
				}

				return null;
			};

			var screenshotId = findFirstScreenshotId(arg.files);
			if (arg.mode == 'expected') {
				this.setExpectedScreenshotId(screenshotId);
				this._expectedImageListController.addTemporaryFile(arg.files);
			} else {
				this.setActualScreenshotId(screenshotId);
				this._actualImageListController.addTemporaryFile(arg.files);
			}

			this._showResult();
		},

		'{rootElement} screenshotSelect': function(context) {
			var args = context.evArg;
			if (args.mode == 'expected') {
				this.setExpected(args);
			} else {
				this.setActual(args);
			}

			this._showResult();
		},

		'{rootElement} selectExecution': function(context) {
		// Do nothing
		},

		'{rootElement} dividerTrackend': function() {
			this._fileUploadController.resetPosition();
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
