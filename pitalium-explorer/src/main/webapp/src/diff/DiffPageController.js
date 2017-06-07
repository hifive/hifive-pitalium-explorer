/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
	/**
	 * This class is a controller for the test result diff page.
	 * 
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name DiffPageController
	 */
	var diffPageController = {
		__name: 'hifive.pitalium.explorer.controller.DiffPageController',

		/**
		 * The &quot;Logic&quot; class
		 * 
		 * @type Logic
		 * @memberOf hifive.pitalium.explorer.controller.DiffPageController
		 */
		_testResultDiffLogic: hifive.pitalium.explorer.logic.TestResultDiffLogic,

		_dividedboxController: h5.ui.components.DividedBox.DividedBox,

		_infoController: hifive.pitalium.explorer.controller.InfoController,

		_screenshotListController: hifive.pitalium.explorer.controller.ScreenshotListController,

		_testResultDiffController: hifive.pitalium.explorer.controller.TestResultDiffController,

		/** current result screenshot id */
		_currentScreenshotId: null,
		_currentExpectedScreenshotId: null,

		_screenshot: null,

		__meta: {
			_infoController: {
				rootElement: '#info'
			},
			_dividedboxController: {
				rootElement: '.dividedbox'
			},
			_screenshotListController: {
				rootElement: '#list'
			},
			_testResultDiffController: {
				rootElement: '#main'
			}
		},

		/**
		 * Called after the controller has been initialized.<br>
		 * Get the id of the right screenshot, and update views.
		 * 
		 * @memberOf hifive.pitalium.explorer.controller.DiffPageController
		 */
		__ready: function() {
			// Get the id of the test result from url query parameters.
			var queryParams = hifive.pitalium.explorer.utils.getParameters();
			if (!queryParams.hasOwnProperty('id')) {
				alert('ID not found');
				return;
			}

			var id = queryParams.id;
			this._currentScreenshotId = id;

			// Get screenshot details
			this._testResultDiffLogic.getScreenshot(id).done(
					this.own(function(screenshot) {
						this._screenshot = screenshot;
						var expectedScreenshotId = screenshot.expectedScreenshotId;
						this._currentExpectedScreenshotId = expectedScreenshotId;

						var diffPromise = null;
						if (expectedScreenshotId != null) {
							this._testResultDiffLogic.getScreenshot(expectedScreenshotId).done(
									this.own(function(expectedScreenshot) {
										diffPromise = this._testResultDiffController.showResult(
												screenshot, expectedScreenshot);
										this._infoController.showInfo(screenshot,
												expectedScreenshot);
									}));
						} else {
							diffPromise = this._testResultDiffController.showResult(screenshot,
									null);
							this._infoController.showInfo(screenshot, null);
						}

						var listPromise = this._screenshotListController.showList(screenshot);

						h5.async.when(diffPromise, listPromise).done(this.own(this._refreshView));
					}));
		},

		_refreshView: function() {
			var $root = $(this.rootElement);
			$root.height(0); // 高さを一度リセット
			var mainHeight = this.$find('#main')[0].scrollHeight;
			var listHeight = this.$find('#list')[0].scrollHeight;
			$(this.rootElement).height(Math.max(mainHeight, listHeight));
			this._dividedboxController.refresh();
		},

		'#info selectExecution': function(context, $el) {
			this._screenshotListController.showList(context.evArg.screenshot);
		},

		'#info updateTargetResult': function(context) {
			var promise = this._testResultDiffLogic.updateTargetResult({
				result: context.evArg.result,
				comment: context.evArg.comment,
				screenshotId: this._currentScreenshotId,
				targetId: this._targetId
			});

			var indicator = this.indicator({
				message: '更新中...',
				promises: promise,
				target: document.body
			}).show();

			promise.done(this.own(function() {
				this._infoController.updateComparisonResult(context.evArg.result === '0');
			}));
			;
		},

		'#list selectScreenshot': function(context, $el) {
			var id = context.evArg.id;
			var expectedId = context.evArg.expectedId;
			if (this._currentScreenshotId == id && this._currentExpectedScreenshotId == expectedId) {
				return;
			}

			this._testResultDiffLogic.getScreenshot(id).done(
					this.own(function(screenshot) {
						// expectedの値を書き換える
						screenshot.expectedScreenshotId = expectedId;
						// 比較結果を書き換える
						if (expectedId == null) {
							this._testResultDiffController.showResult(screenshot, null);
							this._infoController.showInfo(screenshot, null);
						} else {
							this._testResultDiffLogic.getScreenshot(expectedId).done(
									this.own(function(expectedScreenshot) {
										this._testResultDiffController.showResult(screenshot,
												expectedScreenshot);
										this._infoController.showInfo(screenshot,
												expectedScreenshot);
									}));
						}
						this._currentScreenshotId = id;
						this._currentExpectedScreenshotId = expectedId;
					}));
		},

		'#main viewChanged': function(context, $el) {
			this._refreshView();
		},

		'#main updateComparisonResult': function(context, $el) {
			this._targetId = context.evArg.targetId;
			this._infoController.updateComparisonResult(context.evArg.comparisonResult);
		},

		'{window} [resize]': function() {
			this._refreshView();
		},

		'{rootElement} boxSizeChange': function() {
			var height = this.$find('#main')[0].scrollHeight;
			$(this.rootElement).height(height);
		}
	};

	h5.core.expose(diffPageController);

})(jQuery);