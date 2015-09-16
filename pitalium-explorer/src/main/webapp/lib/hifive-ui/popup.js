/*
 * Copyright (C) 2013-2014 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
(function($) {

	/** ポップアップのヘッダ */
	var POPUP_HEADER_TEMPLATE_ID = 'header-template-id';
	h5.core.view.register(POPUP_HEADER_TEMPLATE_ID, '<div class="popupHeader"><h1></h1><div class="popupCloseBtn btn btn-danger"><span class="icon-remove">[%= h5.env.ua.isIE && h5.env.ua.browserVersion < 9 ? "×" : ""  %]</span></div></div>');

	/** ポップアップのコンテンツ */
	var POPUP_CONTENTS = '<div class="popupContents">';
	/** オーバレイのクラス */
	var CLASS_OVERLAY = 'h5PopupOverlay';

	/** ポップアップの位置指定文字列 */
	var POSITION_CONSTRAINT = {
		top: 'top',
		mid: 'mid',
		bottom: 'bottom',
		left: 'left',
		center: 'center',
		right: 'right'
	};

	var popupGroupMap = {};

	// 開いた順にポップアップのグループ名を保持する配列
	var popupGroupOrderArray = [];

	var popupDefaultParam = {
		position: 'absolute',
		draggable: false,
		header: true
	};

	var popupDefaultPositon = {
		top: 'mid',
		left: 'center',
		position: {}
	};

	/**
	 * ポップアップをドラッグ可能にするためのコントローラ
	 */
	var draggableController = {
		_popup: null,
		_$target: null,
		_preX: 0,
		_preY: 0,
		/**
		 * 内部使用のためexposeはしていない
		 */
		__name: 'h5.ui.popupManager._draggableController',
		__construct: function(context) {
			this._popup = context.args.popup;
//			this._popup.promise.done(this.own(function() {
//				// closeされたらdispose
//				this.dispose();
//			}));
		},

		'.popupDragger h5trackstart': function() {
			this._$target = $(this.rootElement);
			var pos = this._$target.offset();
			this._preX = pos.left;
			this._preY = pos.top;
		},
		'.popupDragger h5trackmove': function(context) {
			var dx = context.event.dx;
			var dy = context.event.dy;
			this._$target.css({
				left: this._preX += dx,
				top: this._preY += dy
			});
		},
		'.popupDragger h5trackend': function() {

		}

	};

	/**
	 * 引数が文字列かどうかを判定します。(hifiveから引用)
	 *
	 * @private
	 * @param {Any} target 値
	 * @returns {boolean} 文字列ならtrue、そうでないならfalse
	 */
	function isString(target) {
		return typeof target === 'string';
	}

	function parseHTML(str, context, keepScripts) {
		if ($.parseHTML) {
			return $.parseHTML(str, context, keepScripts)[0];
		}
		return $(str)[0];
	}

	function removeGroupOrder(group) {
		var thisIndex = $.inArray(group, popupGroupOrderArray);
		if (thisIndex !== -1) {
			popupGroupOrderArray.splice(thisIndex, 1);
		}
	}

	function movePopup(target, position) {
		var bw = $(window).width();
		var bh = $(window).height();

		var $target = $(target);

		var cw = $target.outerWidth();
		var ch = $target.outerHeight();

		position = position || $.extend({}, popupDefaultPositon);

		var hMargin = 0;
		var vMargin = 0;
		if (position.position.top) {
			hMargin = position.position.top;
		} else if (position.top === POSITION_CONSTRAINT.mid) {
			hMargin = (bh - ch) / 2;
		} else if (position.top === POSITION_CONSTRAINT.top) {
			hMargin = 0;
		} else if (position.bottom = POSITION_CONSTRAINT.bottom) {
			hMargin = bh - ch;
		}

		if (position.position.left) {
			vMargin = position.position.left;
		} else if (position.left === POSITION_CONSTRAINT.center) {
			vMargin = (bw - cw) / 2;
		} else if (position.left === POSITION_CONSTRAINT.left) {
			vMargin = 0;
		} else if (position.left = POSITION_CONSTRAINT.right) {
			vMargin = bw - cw;
		}
		var scrollX = $(window).scrollLeft();
		var scrollY = $(window).scrollTop();
		$target.css({
			left: scrollX + vMargin,
			top: scrollY + hMargin
		});
	}

	function popupCloseHandler(event, args) {
		var popup = event.data;
		if (popup) {
			// トリガーで引数が渡されたらcloseに渡す
			popup.close(args);
		}
	}

	function showOverlay() {
		// すでに表示済みなら何もしない
		if ($('.' + CLASS_OVERLAY).length) {
			return;
		}
		var overlay = $('<div>').addClass(CLASS_OVERLAY);
		$(document.body).append(overlay);
			// bodyの大きさがwindowより小さかったらwindowに合わせる
		overlay.css({
			width: Math.max($(document.body).innerWidth(), $(window).width()),
			height: Math.max($(document.body).innerHeight(), $(window).height())
		});
	}

	function removeOverlay() {
		$('.' + CLASS_OVERLAY).remove();
	}

	function Popup(rootElement, group, title, contents, controller, option) {
		this.rootElement = rootElement;
		this.group = group;

		this._isShowing = false;
		var $root = $(rootElement);

		if (option.header === true) {
			this.header = parseHTML(h5.core.view.get(POPUP_HEADER_TEMPLATE_ID));
			$root.append(this.header);
			// TODO この形式は1.6で動かない
			// ポップアップの閉じるボタンが押された時のイベントハンドラ
			$root.one('click', '.popupCloseBtn', this, popupCloseHandler);
		}

		this.contents = parseHTML(POPUP_CONTENTS);
		$root.append(this.contents);

		this._controller = null;

		// titleが指定されていればsetTitle
		if (title) {
			this.setTitle(title);
		}

		// contentsまたはcontrollerが指定されていればsetContentsする
		this.setContents(contents, controller);

		this._position = $.extend(true, {}, popupDefaultPositon);

		this._deferred = h5.async.deferred();
		this.promise = this._deferred.promise();

		if (option.draggable === true) {
			// ポップアップをドラッグ可能にする。
			// ヘッダ部分がデフォルトでドラッグ可能であるが、
			// popupDraggerクラスが指定されている要素がコンテンツにあればその要素からでもドラッグできる
			$root.find('.popupHeader').addClass('popupDragger');

			// ドラッグ用のコントローラをバインドする
			h5.core.controller(this.rootElement, draggableController, {
				popup: this
			});
		}
	}

	$.extend(Popup.prototype,
			{
				show: function(option) {
					if (this._isShowing) {
						return;
					}
					this._isShowing = true;
					option = option || {};

					// 表示の順序を更新する
					removeGroupOrder(this.group);
					popupGroupOrderArray.push(this.group);

					// オーバレイの表示(デフォルトは表示、falseが指定されていたら非表示)
					this._overlay = option.overlay !== false;
					if (this._overlay) {
						showOverlay();
						// 他のポップアップからcurrentクラスを外す
						for (var group in popupGroupMap) {
							if (group === this.group) {
								// 開こうとしているポップアップは無視する
								continue;
							}
							var p = popupGroupMap[group];
							$(p.rootElement).removeClass('current');
							p._current = false;
						}
					}

					var $root = $(this.rootElement);
					$root.addClass('current');
					this._current = true;
					$root.css('visibility', 'visible');
					this.refresh();

					// イベントをあげる
					$(this.rootElement).trigger('popupOpened', this);
				},

				hide: function() {
					if (!this._isShowing) {
						return;
					}
					this._isShowing = false;

					$(this.rootElement).css('visibility', 'hidden');

					// overlayの上に表示するポップアップにcurrentクラスを付ける
					for (var i = popupGroupOrderArray.length- 1; i >= 0; i--) {
						var p = popupGroupMap[popupGroupOrderArray[i]];
						if (p._isShowing) {
							if (p._current) {
								break;
							}

							$(p.rootElement).addClass('current');
							p._current = true;
							if (p._overlay) {
								break;
							}
						}

						if (i === 0) {
							// すべてoverlay = falseならばオーバーレイを除去する
							removeOverlay();
						}
					}
				},

				close: function(args) {
					this.hide();

					// イベントをあげる(要素を削除する前じゃないとイベントが拾われない)
					$(this.rootElement).trigger('popupClosed', args);

					// ポップアップを削除
					$(this.rootElement).remove();

					// コントローラのdispose
					this._disposeContentsController();

					// リーク対策
					this.rootElement = null;
					this.contents = null;

					delete popupGroupMap[this.group];
					removeGroupOrder(this.group);

					// プロミスから登録されたdoneハンドラを実行
					this._deferred.resolve(args);
				},

				getSize: function() {
					var $root = $(this.rootElement);
					var w = $root.innerWidth();
					var h = $root.innerHeight();
					return {
						width: w,
						height: h
					};
				},

				setTitle: function(title) {
					$(this.header).find('h1').text(title);
				},

				getTitle: function() {
					return $(this.header).find('h1').text();
				},

				setContentsSize: function(width, height) {
					$(this.contents).css({
						width: width,
						height: height
					});
				},

				getContentsSize: function() {
					var $contents = $(this.contents);
					return {
						width: $contents.widht(),
						height: $contents.height()
					};
				},

				setContents: function(contents, controller) {
					// コントローラをアンバインド
					this._disposeContentsController();
					// コンテンツをセット
					if (isString(contents)) {
						// contentsが文字列の場合はhtmlで追加
						$(this.contents).html(contents);
					} else if (contents
							&& (contents instanceof $ || typeof contents.nodeType === 'number')) {
						// コンテンツがjQueryオブジェクトまたはDOMオブジェクトの場合はappend
						$(this.contents).html($(contents).clone());
					}
					$(this.contents).html(contents);
					if (this._isShowing) {
						this.refresh();
					}
					// コントローラが渡されたらコンテンツ部分にバインド
					// (コントローラはsetContentsを使ってのみセットできる。setContentsControllerは用意していない)
					if (controller) {
						this._controller = h5.core.controller(this.contents, controller, {
							popup: this
						});
					}
				},

				getContents: function() {
					return $(this.contents);
				},

				setPosition: function(constraint, options) {
					// 位置の指定
					if (constraint) {
						var tmp = constraint.split('-');
						var top,left;
						if (tmp.length === 1) {
							if (tmp[0] == POSITION_CONSTRAINT.top
									|| tmp[0] == POSITION_CONSTRAINT.mid
									|| tmp[0] == POSITION_CONSTRAINT.bottom) {
								top = tmp[0];
								left = POSITION_CONSTRAINT.center;
							} else {
								left = tmp[0];
								top = POSITION_CONSTRAINT.mid;
							}
						} else {
							top = tmp[0];
							left = tmp[1];
						}
						this._position.top = top;
						this._position.left = left;
					}
					if (options) {
						$.extend(this._position.position, options, options);
					}

					if (this._isShowing) {
						this.refresh();
					}
				},

				refresh: function() {
					movePopup(this.rootElement, this._position);
				},

				_disposeContentsController: function() {
					// 内部のコントローラを全てdisposeする
					// disposeでPromiseが返ってきたら、removeはdispose完了後にする。
					var controllers = h5.core.controllerManager.getControllers(this.rootElement, {
						deep: true
					});
					for ( var i = 0, len = controllers.length; i < len; i++) {
						controllers[i].dispose();
					}
					this._controller = null;
				}
			});

	/**
	 * ポップアップを作成します。作成した時点では表示されません。<br>
	 * 同じグループのポップアップは同時に1つだけ存在します。 (同一グループのものを開くと、現在開いているものが消えます。)
	 *
	 * @param {String} group グループ名
	 * @param {String} title タイトル名
	 * @param {String} [contents] ポップアップに表示するコンテンツ
	 * @param {Controller} [controller] コンテンツにバインドするコントローラ
	 * @param {String} [param] オプションパラメータ
	 */
	function createPopup(group, title, contents, controller, param) {
		var actualParam = $.extend({}, popupDefaultParam, param);

		// z-indexはcssで指定してある
		var elem = $('<div class="h5Popup">').css({
			position: actualParam.position,
			visibility: 'hidden'
		})[0];
		$(document.body).append(elem);
		var p = new Popup(elem, group, title, contents, controller, actualParam);

		if (isString(group)) {
			var lastPopup = popupGroupMap[group];
			if (lastPopup) {
				lastPopup.close();
				removeGroupOrder(this.group);
			}
			popupGroupMap[group] = p;
		}

		return p;
	}

	function getPopup(group) {
		return popupGroupMap[group];
	}

	h5.u.obj.expose('h5.ui.popupManager', {
		createPopup: createPopup,
		getPopup: getPopup
	});
})(jQuery);