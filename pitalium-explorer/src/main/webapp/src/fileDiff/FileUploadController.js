/*
 * Copyright (C) 2015-2017 NS Solutions Corporation, All Rights Reserved.
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

		'_$target': null,

		'_$root': null,
		'_$container': null,
		'_$fileUpload': null,

		'_lastDraggingTime': null,

		'_checkTimerId': null,
		'_leaveTimerId': null,

		'__ready': function() {
			this._$root = $(this.rootElement);
			this._$container = this.$find('.file-upload-container');
			this._$fileUpload = this.$find('.file-upload');

			this._$root.hide();
		},

		'setTarget': function($target) {
			this._$target = $target;
			this.resetPosition();
		},

		'resetPosition': function() {
			var offset = this._$target.offset();
			this._$container.css({
				'top': offset.top,
				'left': offset.left,
				'width': this._$target.width(),
				'height': this._$target.height()
			});
		},

		/**
		 * ドラッグイベント発生時に記録したtimeをタイマーで定期チェックし、一定間隔以上開いた場合ドラッグ領域の表示を終了する。
		 */
		'_checkDragging': function() {
			this._checkTimerId = setTimeout(this.own(function() {
				this._checkTimerId = null;

				if (!this._lastDraggingTime) {
					return;
				}

				var now = $.now();
				if (now - this._lastDraggingTime > 100) {
					this._dragEnd();
					return;
				}

				this._checkDragging();
			}), 100);
		},

		'dragStart': function(context, $el) {
			context.event.stopPropagation();
			context.event.preventDefault();

			if (this._dragging) {
				return;
			}

			// ファイルドラッグか否かをチェック
			var types = context.event.originalEvent.dataTransfer.types;
			if (types && $.inArray('Files', types) == -1) {
				return;
			}

			// ドラッグ領域を表示する
			this._$root.show();
			this._lastDraggingTime = $.now();

			if (!this._checkTimerId) {
				this._checkDragging();
			}
		},

		'dragLeaved': function() {
			if (this._leaveTimerId) {
				window.clearTimeout(this._leaveTimerId);
			}

			// 画面内要素の出入りでdragleaveが発生するため、遅延チェック
			this._leaveTimerId = setTimeout(this.own(this._dragEnd), 100);
		},

		'_dragEnd': function() {
			this._dragging = false;
			this._lastDraggingTime = null;

			this._$root.hide();
			this._$fileUpload.removeClass('active');
		},

		'{rootElement} dragover': function(context, $el) {
			context.event.stopPropagation();
			context.event.preventDefault();

			if (this._leaveTimerId) {
				window.clearTimeout(this._leaveTimerId);
				this._leaveTimerId = null;
			}

			this._lastDraggingTime = $.now();
		},

		'.file-upload dragover': function(context, $el) {
			context.event.stopPropagation();
			context.event.preventDefault();

			this._$fileUpload.removeClass('active');
			$el.addClass('active');

			if (this._leaveTimerId) {
				window.clearTimeout(this._leaveTimerId);
				this._leaveTimerId = null;
			}

			this._lastDraggingTime = $.now();
		},

		'.file-upload dragleave': function(context, $el) {
			context.event.stopPropagation();
			context.event.preventDefault();

			this._$fileUpload.removeClass('active');
		},

		'{rootElement} drop': function(context) {
			context.event.stopPropagation();
			context.event.preventDefault();

			this._dragEnd();
		},

		'.file-upload drop': function(context, $el) {
			context.event.stopPropagation();
			context.event.preventDefault();

			this._dragEnd();

			var expected = $el.hasClass('expected');

			var dataTransfer = context.event.originalEvent.dataTransfer;
			if (!dataTransfer.files) {
				return;
			}

			this._fileUploadLogic.upload(dataTransfer).done(this.own(function(files) {
				if (!files || files.length == 0) {
					alert("画像ファイルが検出されませんでした。\nフォルダのアップロードはChromeのみ対応しています。");
					return;
				}

				this.trigger('uploadFile', {
					'files': files,
					'mode': expected ? 'expected' : 'actual'
				});
			}));
		}

	};

	h5.core.expose(fileUploadController);
})(jQuery);
