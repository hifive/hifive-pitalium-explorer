/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */

(function($) {

	/**
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name FileUploadController
	 */
	var fileUploadController = {

		/**
		 * @memberOf hifive.pitalium.explorer.controller.FileUploadController
		 */
		'__name': 'hifive.pitalium.explorer.controller.FileUploadController',

		/**
		 * The &quot;Logic&quot; class
		 *
		 * @type Logic
		 * @memberOf hifive.pitalium.explorer.controller.FileUploadController
		 */
		'_logic': hifive.pitalium.explorer.logic.FileUploadLogic,

		'_dragging': false,

		'_$rootElement': null,
		'_$containerElement': null,
		'_$fileUploadElement': null,

		'__construct': function() {
			this.log.debug('FileUploadController construct');
		},

		'__init': function() {
			this.log.debug('FileUploadController init');
		},

		'__postInit': function() {
			this.log.debug('FileUploadController postInit');
		},

		'__ready': function() {
			this.log.debug('FileUploadController ready');

			this._$rootElement = $(this.rootElement);
			this._$containerElement = this.$find('.file-upload-container');
			this._$fileUploadElement = this.$find('.file-upload');

			this._$containerElement.hide();
		},

		'{rootElement} dragenter': function(context, $el) {
			this.log.debug('root enter');

			context.event.stopPropagation();
			context.event.preventDefault();

			if (this._dragging) {
				return;
			}

			// ドラッグ領域を前面に移動し、ドラッグ領域を表示する
			this._$rootElement.addClass('container-over');
			this._$containerElement.show();
		},

		'{rootElement} dragleave': function(context, $el) {
			this.log.debug('root leave');

			context.event.stopPropagation();
			context.event.preventDefault();
		},

		'.file-upload-container dragenter': function(context, $el) {
			this.log.debug('container enter');

			context.event.stopPropagation();
			context.event.preventDefault();

			this._dragging = true;
		},

		'.file-upload-container dragleave': function(context, $el) {
			this.log.debug('container leave');

			context.event.stopPropagation();
			context.event.preventDefault();

			this._dragging = false;

			// ドラッグ領域を背面に移動し、ドラッグ領域を非表示にする
			this._$rootElement.removeClass('container-over');
			this._$containerElement.hide();
		},

		'.file-upload dragover': function(context, $el) {
			context.event.stopPropagation();
			context.event.preventDefault();

			this._$fileUploadElement.removeClass('active');
			$el.addClass('active');
		},

		'.file-upload dragleave': function(context, $el) {
			context.event.stopPropagation();
			context.event.preventDefault();

			this._$fileUploadElement.removeClass('active');
		},

		'.file-upload drop': function(context, $el) {
			context.event.stopPropagation();
			context.event.preventDefault();

			this._dragging = false;

			this._$rootElement.removeClass('container-over');
			this._$containerElement.hide();
			this._$fileUploadElement.removeClass('active');

			var expected = $el.hasClass('expected');

			var files = context.event.originalEvent.dataTransfer.files;
			if (!files) {
				return;
			}

			if (files.length != 1) {
				alert('ドラッグするファイルは一つだけにして下さい。');
				return;
			}

			var form = new FormData();
			form.append('files', files[0]);

			h5.ajax({
				url: 'files/upload',
				type: 'POST',
				data: form,
				processData: false,
				contentType: false
			}).done(this.own(function(result) {
				var screenshotId = result[0];
				this.trigger('ptlFileUploaded', {
					'screenshotId': screenshotId,
					'mode': expected ? 'expected' : 'actual'
				});
			}));

		},

		'{window} dragenter': function() {
			this.log.debug('window enter');
		},

		'{window} dragleave': function() {
			this.log.debug('window dragleave');
		}


	};

	h5.core.expose(fileUploadController);
})(jQuery);
