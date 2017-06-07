/*global h5, hifive, window, document */
/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
	/**
	 * This class is a controller for the list of screeenshots.
	 * 
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name UpdateResultController
	 */
	var updateResultController = {
		__name: 'hifive.pitalium.explorer.controller.UpdateResultController',


		__init: function(context) {
			this._popup = context.args.popup;
			this._orgResult = this.$find('[name="result"]:checked').val();
		},

		'.ok click': function() {
			context.event.preventDefault();
			this._popup.close({
				result: this.$find('[name="result"]:checked').val(),
				comment: this.$find('[name="comment"]').val()
			});
		},

		'.cancel click': function() {
			this._popup.close();
		}
	};

	h5.core.expose(updateResultController);
})(jQuery);