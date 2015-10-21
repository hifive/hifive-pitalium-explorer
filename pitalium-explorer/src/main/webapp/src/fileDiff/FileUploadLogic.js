/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */

(function($) {

	/**
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
		 * Upload dropped files.
		 *
		 * @param {DataTransfer} dataTransfer
		 * @returns {JqXHRWrapper}
		 * @method
		 * @memberOf hifive.pitalium.explorer.logic.FileUploadLogic
		 */
		'upload': function(dataTransfer) {
			var d = this.deferred();

			this._createUploadArgument(dataTransfer).done(this.own(function(files) {
				var form = new FormData();
				var requestFiles = [];
				var collectFiles = function(file) {
					if (file.isFile) {
						form.append('files', file.file);
						requestFiles.push(file);
						return;
					}

					file.children.forEach(collectFiles);
				};
				files.forEach(collectFiles);

				// Empty => return empty array
				if (requestFiles.length == 0) {
					d.resolve([]);
					return;
				}

				h5.ajax({
					url: 'files/upload',
					type: 'POST',
					data: form,
					processData: false,
					contentType: false
				}).done(function(ids) {
					for (var i = 0; i < ids.length; i++) {
						var file = requestFiles[i];
						file.screenshotId = ids[i];
						delete file.file;
					}

					d.resolve(files);
				});
			}));

			return d.promise();
		},

		/**
		 * @param {DataTransfer} dataTransfer
		 * @return {Array}
		 * @method
		 * @memberOf hifive.pitalium.explorer.logic.FileUploadLogic
		 */
		'_createUploadArgument': function(dataTransfer) {
			// DataTransfer#items is only implemented by Chrome (Webkit/Blink)
			// Directory upload function is disabled on IE and Firefox.
			if (!dataTransfer.items) {
				return this._createUploadArgumentCompat(dataTransfer);
			}

			var result = [];
			var items = dataTransfer.items;
			var files = dataTransfer.files;

			var d = this.deferred();
			var dp = [];
			for (var i = 0; i < items.length; i++) {
				var item = items[i];
				if (item.kind !== 'file') {
					continue;
				}

				var entry;
				if (item.webkitGetAsEntry) {
					entry = item.webkitGetAsEntry();
				} else {
					entry = item.getAsEntry();
				}

				if (entry.isDirectory) {
					dp.push(this._createUploadDirectoryArgument(entry));
					continue;
				}

				var file = this._createUploadFileArgument(files[i]);
				if (file) {
					result.push(file);
				}
			}

			h5.async.when(dp).done(function() {
				for (var i = 0; i < arguments.length; i++) {
					result.push(arguments[i]);
				}
				d.resolve(result);
			});

			return d.promise();
		},

		/**
		 * 
		 * @param {DataTransfer} dataTransfer
		 * @return {JqXHRWrapper}
		 * @method
		 * @memberOf hifive.pitalium.explorer.logic.FileUploadLogic
		 */
		'_createUploadArgumentCompat': function(dataTransfer) {
			var d = this.deferred();

			var result = [];
			var files = dataTransfer.files;
			for (var i = 0; i < files.length; i++) {
				var file = this._createUploadFileArgument(files[i]);
				if (file) {
					result.push(file);
				}
			}

			d.resolve(result);
			return d.promise();
		},

		/**
		 * @param {File} file
		 * @return {Object}
		 * @method
		 * @memberOf hifive.pitalium.explorer.logic.FileUploadLogic
		 */
		'_createUploadFileArgument': function(file) {
			// Only images
			if (!file.type.match(/^image\//)) {
				return null;
			}

			return {
				'type': 'file',
				'isFile': true,
				'isDirectory': false,
				'name': file.name,
				'file': file
			};
		},

		/**
		 *
		 * @param {DirectoryEntry} directory
		 * @return {JqXHRWrapper}
		 * @method
		 * @memberOf hifive.pitalium.explorer.logic.FileUploadLogic
		 */
		'_createUploadDirectoryArgument': function(directory) {
			var result = {
				'type': 'directory',
				'isFile': false,
				'isDirectory': true,
				'name': directory.name,
				'children': []
			};

			var d = this.deferred();
			var reader = directory.createReader();

			// Continue calling readEntries() until an empty array is returned.
			// You have to do this because the API might not return all entries in a single call.
			// https://developer.mozilla.org/ja/docs/Web/API/DirectoryReader
			var readEntries = this.own(function() {
				// readEntries callback is called asynchronously
				reader.readEntries(this.own(function(entries) {
					if (!entries.length) {
						d.resolve(result);
						return;
					}

					var promises = [];
					for (var i = 0; i < entries.length; i++) {
						var entry = entries[i];
						if (entry.isDirectory) {
							promises.push(this._createUploadDirectoryArgument(entry));
							continue;
						}

						var df = this.deferred();
						promises.push(df);
						(function(d, createUploadFileArgument) {
							entry.file(function(f) {
								d.resolve(createUploadFileArgument(f));
							});
						})(df, this._createUploadFileArgument);
					}

					h5.async.when(promises).done(function() {
						for (var i = 0; i < arguments.length; i++) {
							if (arguments[i]) {
								result.children.push(arguments[i]);
							}
						}
						readEntries();
					})
				}), this.own(function() {
					this.log.debug('read directory failed');
				}));
			});
			readEntries();

			return d.promise();
		}

	};

	h5.core.expose(fileUploadLogic);
})(jQuery);