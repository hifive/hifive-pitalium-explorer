/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */

(function($) {

	/**
	 * @class
	 * @memberOf hifive.pitalium.explorer.controller
	 * @name ImageListController
	 */
	var imageListController = {
		'__name': 'h5.pitalium.explorer.controller.ImageListController',

		'_mode': null,
		'_$tree': null,
		'_treeData': null,
		'_uploadImageList': null,

		'__ready': function() {
			this._mode = $(this.rootElement).hasClass('expected') ? 'expected' : 'actual';
			this._$tree = this.$find('.tree-root');

			this._init();
		},

		'_init': function() {
			this._uploadImageList = {
				'text': 'Uploaded images',
				'state': {
					'opened': true
				},
				'children': [],
				'a_attr': {
					'data-screenshot-type': 'directory'
				}
			};
			this._treeData = [this._uploadImageList];

			this._refreshTree();
		},

		'_refreshTree': function() {
			if (this._$tree.hasClass('jstree')) {
				this._$tree.jstree(true).destroy(true);
			}

			var data = this._treeData;
			this._$tree.jstree({
				'core': {
					'data': data
				}
			})
		},

		'mode': function() {
			return this._mode;
		},

		'addTemporaryFile': function(file) {
			this._uploadImageList.children.forEach(function(el) {
				if (el.state) {
					el.state.selected = false;
				}
			});
			this._uploadImageList.children.push({
				'text': file.fileName,
				'icon': false,
				'state': {
					'selected': true
				},
				'a_attr': {
					'class': 'screenshot',
					'data-screenshot-type': 'temporary',
					'data-screenshot-id': file.screenshotId
				}
			});
			this._refreshTree();
		},

		'.screenshot click': function(context, $el) {
			var screenshotId = $el.data('screenshotId');
			var mode = this._mode;
			this.trigger('screenshotSelect', {
				'screenshotId': screenshotId,
				'mode': mode
			});
		}
	};

	h5.core.expose(imageListController);
})(jQuery);