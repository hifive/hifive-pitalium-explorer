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
		'_fileUploadLogic': hifive.pitalium.explorer.logic.FileUploadLogic,

		'_dragging': false,

		'_$rootElement': null,
		'_$containerElement': null,
		'_$fileUploadElement': null,

		'__ready': function() {
			this._$rootElement = $(this.rootElement);
			this._$containerElement = this.$find('.file-upload-container');
			this._$fileUploadElement = this.$find('.file-upload');

			this._$containerElement.hide();
		},

		'dragStart': function(context, $el) {
			context.event.stopPropagation();
			context.event.preventDefault();

			if (this._dragging) {
				return;
			}

			// ドラッグ領域を前面に移動し、ドラッグ領域を表示する
			this._$rootElement.addClass('container-over');
			this._$containerElement.show();
		},

		'.file-upload-container dragenter': function(context, $el) {
			context.event.stopPropagation();
			context.event.preventDefault();

			this._dragging = true;
		},

		'.file-upload-container dragleave': function(context, $el) {
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

			this._fileUploadLogic.upload(files[0]).done(this.own(function(data) {
				data.mode = expected ? 'expected' : 'actual';
				this.trigger('uploadFile', data);
			}));
		}

	};

	h5.core.expose(fileUploadController);
})(jQuery);
