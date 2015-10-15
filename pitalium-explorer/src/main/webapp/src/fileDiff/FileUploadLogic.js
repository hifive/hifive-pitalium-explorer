/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */

(function($) {

	/**
	 *
	 * @class
	 * @memberOf hifive.pitalium.explorer.logic
	 * @name FileUploadLogic
	 */
	var fileUploadLogic = {
		/**
		 * @memberOf hifive.pitalium.explorer.logic.FileUploadLogic
		 */
		'__name': 'hifive.pitalium.explorer.logic.FileUploadLogic',

		/**
		 * 
		 * @param {File} file
		 * @returns {JqXHRWrapper}
		 * @method
		 * @memberOf hifive.pitalium.explorer.logic.FileUploadLogic
		 */
		'upload': function(file) {
			var d = this.deferred();

			var form = new FormData();
			form.append('files', file);

			h5.ajax({
				url: 'files/upload',
				type: 'POST',
				data: form,
				processData: false,
				contentType: false
			}).done(function(ids) {
				var id = ids[0];
				d.resolve({
					'screenshotId': id,
					'fileName': file.name
				});
			});

			return d.promise();
		}

	};

	h5.core.expose(fileUploadLogic);
})(jQuery);