/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {

	var SelectExecutionControllerDef = hifive.pitalium.explorer.controller.SelectExecutionController;
	var UpdateResultController = hifive.pitalium.explorer.controller.UpdateResultController;

	/**
	 *
	 */
	var infoController = {
		/**
		 * @memberOf hifive.pitalium.explorer.controller.InfoController
		 */
		__name: 'hifive.pitalium.explorer.controller.InfoController',

		_comparisonResult: null,

		showInfo: function(screenshot, expectedScreenshot) {
			this.view.update('#detail', 'testResultListTemplate', {
				actual: screenshot,
				expected: expectedScreenshot
			});
		},

		updateComparisonResult: function(comparisonResult, update) {
			this._comparisonResult = comparisonResult;
			this.view.update('#comparisonResult', 'comparisonResultTemplate', {
				comparisonResult: comparisonResult,
				update: update
			});
			this._changeTitle(comparisonResult);
		},

		enableUpdateResultButton: function() {
			this.$find('#update_result').removeClass('disabled');
		},

		disableUpdateResultButton: function() {
			this.$find('#update_result').addClass('disabled');
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

		'#select_execution click': function(context, $el) {
			context.event.preventDefault();
			this._showPopup('execution_popup', 'Select an execution', SelectExecutionControllerDef,
					this.own(this._triggerSelectExecution));
		},

		'#update_result:not(:disabled) click': function(context, $el) {
			context.event.preventDefault();
			var $content = this.$find('#update_result_popup');

			var $checkboxes = $content.find('[name="result"]');
			$checkboxes.parent().css('display', 'none');
			$checkboxes.attr('checked', false);

			if (this._comparisonResult != null) {
				var index = this._comparisonResult ? 1 : 0;
				var $targetInput = $checkboxes.eq(index);
				$targetInput.attr('checked', true);
				$targetInput.parent().css('display', '');
			}

			this._showPopup('update_result_popup', "Update this target's result",
					UpdateResultController, this.own(this._updateTargetResult));
		},

		_showPopup: function(popupKey, title, controllerDef, doneHandler) {
			var $content = this.$find('#' + popupKey);

			var popup = h5.ui.popupManager.createPopup(popupKey, title, $content.html(),
					controllerDef, {
						draggable: true
					});
			popup.promise.done(doneHandler);
			popup.setContentsSize(600, null);
			popup.show();
		},

		_triggerSelectExecution: function(screenshot) {
			this.trigger('selectExecution', {
				screenshot: screenshot
			});
		},

		_updateTargetResult: function(args) {
			if (!args) {
				return;
			}

			this.trigger('updateTargetResult', args);
		}

	};
	h5.core.expose(infoController);
})(jQuery);