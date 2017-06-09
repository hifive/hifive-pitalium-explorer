/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {

	var SelectExecutionControllerDef = hifive.pitalium.explorer.controller.SelectExecutionController;

	/**
	 *
	 */
	var infoController = {
		/**
		 * @memberOf hifive.pitalium.explorer.controller.InfoController
		 */
		__name: 'hifive.pitalium.explorer.controller.InfoController',

		showInfo: function(screenshot, expectedScreenshot) {
			this.view.update('#detail', 'testResultListTemplate', {
				actual: screenshot,
				expected: expectedScreenshot
			});
		},

		updateComparisonResult: function(comparisonResult) {
			this.view.update('#comparisonResult', 'comparisonResultTemplate', {
				comparisonResult: comparisonResult
			});
			this._changeTitle(comparisonResult);
		},

		_changeTitle: function(comparisonResult) {
			if (this._orgTitle == null) {
				this._$title = $('title');
				this._orgTitle = this._$title.text();
			}

			if (!comparisonResult) {
				this._$title.text(this._orgTitle);
			} else if (comparisonResult) {
				this._$title.text('○ ' + this._orgTitle);
			} else {
				this._$title.text('× ' + this._orgTitle);
			}
		},

		'#select_execution click': function() {
			var popup = h5.ui.popupManager.createPopup('execution', 'Select an execution', this
					.$find('#popup_content').html(), SelectExecutionControllerDef, {
				draggable: true
			});
			popup.promise.done(this.own(this._triggerSelectExecution));
			popup.setContentsSize(600, 550);
			popup.show();
		},

		_triggerSelectExecution: function(screenshot) {
			this.trigger('selectExecution', {
				screenshot: screenshot
			});
		}
	};
	h5.core.expose(infoController);
})(jQuery);