/*
 * Copyright (C) 2012-2014 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
(function() {
	var DATA_STATE = 'state';
	var DATA_DEFAULT_STATE = 'default-state';
	var EVENT_STATE_CHANGE = 'state-change';
	var selectBoxController = {
		__name: 'h5.ui.components.DividedBox.StateBox',
		_currentState: null,
		__init: function() {
			// 初期stateの設定
			var $stateBoxes = this._getAllStateBoxes();
			var defaultState = $(this.rootElement).data(DATA_DEFAULT_STATE);
			if (defaultState) {
				// defaultが設定されている場合はデフォルト
				this.setState(defaultState);
			} else {
				// defaultが設定されていない場合は最初の要素を表示
				this.setState($stateBoxes.data(DATA_STATE));
			}

			// FIXME ルートエレメントからこのコントローラを辿れるようにjQuery.dataを使って覚えさせておく
			// (getControllers()を使ったDOM->Controllerの特定は子コントローラの場合にできないため)
			$(this.rootElement).data('h5controller-statebox-instance', this);
		},
		setState: function(state) {
			if (this._currentState === state) {
				return;
			}
			var $target = this._getStateBoxByState(state);
			if (!$target.length) {
				this.log.warn('指定されたstateの要素はありません。{}', state);
				return;
			}
			var $stateBoxes = this.$find('>*[data-' + DATA_STATE + ']');
			$stateBoxes.css('display', 'none');
			$target.css('display', 'block');
			this._currentState = state;
			this.trigger(EVENT_STATE_CHANGE, state);
		},
		getState: function() {
			return this._currentState;
		},
		getContentsSize: function() {
			var $current = this._getStateBoxByState(this._currentState);
			// TODO outerWidth/Heightかどうかはオプション？
			return {
				width: $current.outerWidth(true),
				height: $current.outerHeight(true)
			};
		},
		_getAllStateBoxes: function() {
			return this.$find('>[data-' + DATA_STATE + ']');
		},
		_getStateBoxByState: function(state) {
			return this.$find('>[data-' + DATA_STATE + '="' + state + '"]');
		},
		__unbind: function() {
			$(this.rootElement).data('h5controller-statebox-instance', null);
		}
	};
	h5.core.expose(selectBoxController);
})();

(function() {
	/** データ属性名：ボックスが隠れているかどうか */
	var DATA_HIDDEN = 'dividedbox-boxhidden';

	/** クラス名：dividedBoxルートに指定。垂直区切り設定 */
	var CLASS_VERTICAL = 'vertical';

	/** クラス名：dividedBoxルートに指定。水平区切り設定 */
	var CLASS_HORIZONTAL = 'horizontal';

	/** クラス名：dividedBoxのルートに指定。初期化時にdividedBoxのサイズをboxのサイズに因らず固定にする。 */
	var CLASS_FREEZE_SIZE = 'freezeSize';

	/** クラス名：boxに指定。dividerによるサイズ変更不可 */
	var CLASS_FIXED_SIZE = 'fixedSize';

	/** クラス名：boxに指定。初期化時にサイズ変更しないボックスに指定。指定されたボックスはバインド前のサイズのまま。 */
	var CLASS_KEEP_SIZE = 'keepSize';

	/** クラス名：divderに指定。操作不可 */
	var CLASS_FIXED_DIVIDER = 'fixedDivider';

	/** dividedBoxによって位置を管理されているboxかどうか(動的に追加される要素についても位置計算時のこのクラスが追加される) */
	var CLASS_MANAGED = 'dividedbox-managed';

	/** クラス名: dividedBoxのルートに追加するクラス名 */
	var CLASS_ROOT = 'dividedBox';

	/** イベント名：ボックスのサイズが変更されたときに上げるイベント */
	var EVENT_BOX_SIZE_CHANGE = 'boxSizeChange';

	/** イベント名：dividerのトラック操作 */
	var EVENT_DIVIDER_TRACK_START = 'dividerTrackstart';
	var EVENT_DIVIDER_TRACK_MOVE = 'dividerTrackmove';
	var EVENT_DIVIDER_TRACK_END = 'dividerTrackend';


	/** データ属性: ボックスの最小サイズ。このデータ属性で指定されたサイズ以下には動かないようになる */
	var DATA_MIN_BOX_SIZE = 'min-size';

	/**
	 * データ属性: 実際のouterWidth|Heightと、dividedBoxが設定したouterSizeが異なってしまう場合に、
	 * dividedBoxが設定したouterSizeを要素に持たせておくためのデータ属性名
	 */
	var DATA_VIRTUAL_OUTER_SIZE = 'virtual-outer-size';

	var dividedBoxController = {

		__name: 'h5.ui.components.DividedBox.DividedBox',

		_dividerPos: {
			left: 0.5,
			top: 0.5
		},

		_type: null,

		_$root: null,

		_lastAdjustAreaWH: null,

		_l_t: '',

		_w_h: '',

		_outerW_H: '',

		_scrollW_H: '',

		__init: function(context) {
			// viewの登録
			this.view
					.register(
							'divider',
							'<div class="divider" style="position:absolute"><div style="height:50%;"></div><div class="dividerHandler"></div></div>');


			var $root = this._$root = $(this.rootElement);
			var type = this._type = $root.hasClass(CLASS_VERTICAL) ? 'y' : 'x';

			// 要素内の空のテキストノードを削除
			this._cleanWhitespace($root[0]);

			var isHorizontal = type === 'x';
			$root.addClass(CLASS_ROOT);
			if (isHorizontal) {
				$root.addClass(CLASS_HORIZONTAL);
			}

			var w_h = this._w_h = isHorizontal ? 'width' : 'height';
			this._l_t = isHorizontal ? 'left' : 'top';

			this._outerW_H = isHorizontal ? 'outerWidth' : 'outerHeight';
			this._scrollW_H = isHorizontal ? 'scrollWidth' : 'scrollHeight';

			// frezeSize指定時はdividedBoxのルートを適用時のサイズに固定
			if ($root.hasClass(CLASS_FREEZE_SIZE)) {
				$root.width($root.width());
				$root.height($root.height());
			}

			var rootPosition = $root.css('position');
			if (rootPosition === 'static' || !rootPosition) {
				// ルートがposition:staticまたは指定無しの場合はposition:relativeを設定
				$root.css('position', 'relative');
				if (h5.env.ua.isOpera) {
					$root.css({
						'top': 0,
						'left': 0
					});
				}
			}

			// 各boxのサイズ設定
			var $boxes = this._getBoxes();
			// 初めてのrefresh(_lastAdjustAreaWHが未設定==__initから呼ばれた最初のrefresh)かどうか
			// boxのサイズ計算
			var totalBoxSize = 0;
			$boxes.each(this.ownWithOrg(function(box) {
				var outerSize = this._getOuterSize(box);
				totalBoxSize += outerSize;
				// サイズを固定する
				this._setOuterSize(box, outerSize);
			}));
			// クラスの設定(position:absoluteにする)
			$boxes.addClass(CLASS_MANAGED);
			// dividerのサイズ計算
			// 追加して削除することで、実際に置いた時のサイズを取得
			var totalDividerSize = 0;
			if ($boxes.length > 1) {
				var $tmpDivider = $('<div class="divider"></div>');
				$(this.rootElement).append($tmpDivider);
				totalDividerSize = $tmpDivider[w_h]() * ($boxes.length - 1);
				$tmpDivider.remove();
			}
			// ボックスの合計サイズとdividerの合計サイズから、現在のルート要素の幅に調整する。
			// ボックスとdividerの合計サイズを覚えておく
			this._lastAdjustAreaWH = totalBoxSize + totalDividerSize;

			// リフレッシュ
			this.refresh();

			// keepSizeクラスはバインド時とinsert時のみ計算で使用するのでここで削除
			$boxes.removeClass(CLASS_KEEP_SIZE);
		},

		/**
		 * ボックスとdividerの位置とサイズを最適化します
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 */
		refresh: function() {
			var type = this._type;

			// ボックスにクラスCLASS_MANAGEDを追加
			// position:absoluteに設定
			// ボックス間に区切り線がない場合は挿入
			var $boxes = this._getBoxes();
			$boxes.not('.' + CLASS_MANAGED).each(this.ownWithOrg(function(box) {
				// _adjustで追加された分のサイズを考慮してボックスのサイズを調整する必要があるため、
				// 新規追加されたボックスのサイズを_lastAdjustWHに加える
				var $box = $(box);
				this._lastAdjustAreaWH += this._getOuterSize($box);
				// 新規追加されたボックスにクラス追加(position:absolute設定)
				$box.addClass(CLASS_MANAGED);
			}));

			// dividerが間にないボックスを探して、ボックス間にdividerを追加
			$boxes.slice(1).each(this.ownWithOrg(function(box, index) {
				var $box = $(box);
				var prevBox = $boxes[index];
				var $prev = $box.prev();
				var hasDivider = false;
				while ($prev.length && !hasDivider && $prev[0] !== prevBox) {
					hasDivider = $prev.is('.divider');
					$prev = $prev.next();
				}
				if (hasDivider) {
					return;
				}
				var $divider = $(this.view.get('divider'));
				$box.before($divider);
			}));

			// dividerとボックスの配置
			var $dividers = this._getDividers();
			var lastIndex = $dividers.length - 1;
			var appearedUnfix = false;
			$dividers.each(this.ownWithOrg(function(divider, index) {
				var isLast = index === lastIndex;
				var $divider = $(divider);
				var isVisibleDivider = $divider.css('display') !== 'none';
				var $prev = this._getPrevBoxByDivider($divider);
				var $next = this._getNextBoxByDivider($divider);

				var nextZIndex = $next.css('z-index');
				if (!nextZIndex || nextZIndex === 'auto') {
					nextZIndex = 0;
				}

				// dividerの位置調整
				var dividerTop = (type === 'y' && $prev.length) ? $prev.position().top
						+ this._getOuterSize($prev) : 0;
				var dividerLeft = (type === 'x' && $prev.length) ? $prev.position().left
						+ this._getOuterSize($prev) : 0;
				$divider.css({
					top: dividerTop,
					left: dividerLeft,
					'z-index': nextZIndex + 1
				});

				// dividerの次の要素の調整
				var nextTop = (type === 'y') ? dividerTop
						+ (isVisibleDivider ? $divider.outerHeight(true) : 0) : 0;
				var nextLeft = (type === 'x') ? dividerLeft
						+ (isVisibleDivider ? $divider.outerWidth(true) : 0) : 0;
				$next.css({
					top: nextTop,
					left: nextLeft
				});

				// 固定dividerの決定
				// 操作するとfixedSizeのboxのサイズが変わってしまうような位置のdividerは操作不可(fixedDivider)にする
				appearedUnfix = appearedUnfix || !$prev.hasClass(CLASS_FIXED_SIZE);
				if (isLast && (!appearedUnfix || $next.hasClass(CLASS_FIXED_SIZE))) {
					// 一番最後まで計算が終わって、かつ、最後のボックスがfixedSizeなら、そこから前にたどって、fixedDividerになるdividerを探す
					// 最後のboxがfixedの時は、そのboxの前のdividerはfixedDivider
					$divider.addClass(CLASS_FIXED_DIVIDER);
					// fixedでないboxが出てくるまで、前のdividerを辿って全てfixedにする
					// 既に今までのチェックでfixedでないboxが無いことが分かっていれば何もしない
					if (!appearedUnfix) {
						return;
					}
					var $p = $prev;
					var $d = $divider;
					while (true) {
						$d.addClass(CLASS_FIXED_DIVIDER);
						$p = this._getPrevBoxByDivider($d);
						if (!$p.hasClass(CLASS_FIXED_SIZE)) {
							break;
						}
						$d = this._getPrevDividerByBox($p);
					}
				} else {
					// 前から辿ってfixedDividerかどうかの判定を行う
					// あるdividerより前のボックスの中にfixedSizeでないボックスが一つもないならそのdividerはfixedDivder
					if (!isLast && !appearedUnfix) {
						$divider.addClass(CLASS_FIXED_DIVIDER);
					} else {
						$divider.removeClass(CLASS_FIXED_DIVIDER);
					}
				}
			}));

			// 以上の配置を元にルート要素サイズに合わせて再配置
			this._adjust();
		},

		/**
		 * ボックスの追加
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {Integer} index 何番目に追加するか(追加した要素が何番目に来るか)
		 * @param {DOM|jQuery} box
		 */
		insert: function(index, box) {
			var $root = this._$root;

			var $target = this._getBoxElement(index);
			if ($target.length) {
				// ボックスの追加
				$target.before(box);
			} else {
				// 範囲外の場合は一番最後に追加
				$root.append(box);
			}
			// ボックスのサイズを固定する(styleでwidht|heightが設定していある状態にする)
			this._setOuterSize(box, this._getOuterSize(box));
			this.refresh();

			// keepSizeクラスはバインド時とinsert時のみ計算で使用するのでここで削除
			$(box).removeClass(CLASS_KEEP_SIZE);
		},

		/**
		 * ボックスの削除
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {Integer} index 何番目のボックスを削除するか
		 */
		remove: function(index) {
			var $target = this._getBoxElement(index);
			if (!$target.length) {
				// ターゲットとなるボックスが無い場合は何もしない
				return;
			}
			var $prevDivider = this._getPrevDividerByBox($target);
			if ($prevDivider.length) {
				$prevDivider.remove();
			} else {
				// 先頭要素だった場合は次の要素を先頭に持ってくる
				var $nextDivider = this._getNextDividerByBox($target);
				var $nextBox = this._getNextBoxByDivider($nextDivider);
				$nextBox.css(this._l_t, $target.css(this._l_t));
				$nextDivider.remove();
			}
			// 削除ボックスのサイズ分、dividedBoxのサイズが小さくなる。
			// dividedBoxのサイズが小さくなった状態から、現在のサイズに変更されたと見做して調整されるようにする
			// そのために前回調整サイズに削除されたボックスのサイズを引いている
			this._lastAdjustAreaWH -= this._getOuterSize($target);
			$target.remove();
			this.refresh();
		},

		/**
		 * ボックスの最小化
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {index|DOM|jQuery|String} box boxのindexまたはbox要素またはセレクタ
		 * @param {Object} opt {@link h5.ui.components.DividedBox.DividedBox#resize}のオプションと同じです
		 */
		minimize: function(box, opt) {
			this.resize(box, 0, opt);
		},

		/**
		 * ボックスの最大化
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {index|DOM|jQuery|String} box boxのindexまたはbox要素またはセレクタ
		 * @param {Object} opt {@link h5.ui.components.DividedBox.DividedBox#resize}のオプションと同じです
		 */
		maximize: function(box, opt) {
			this.resize(box, $(this.rootElement)[this._w_h](), $.extend({}, opt, {
				partition: 0.5
			}));
		},

		/**
		 * ボックスの中身の大きさを自動取得し、そのサイズにリサイズします
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {index|DOM|jQuery|String} box boxのindexまたはbox要素またはセレクタ
		 * @param {Object} opt {@link h5.ui.components.DividedBox.DividedBox#resize}のオプションと同じです
		 */
		fitToContents: function(box, opt) {
			this.resize(box, null, opt);
		},

		/**
		 * {@link h5.ui.components.DividedBox.DividedBox#hide}で非表示にしたボックスを表示します
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {index|DOM|jQuery|String} box boxのindexまたはbox要素またはセレクタ
		 * @param {Object} opt {@link h5.ui.components.DividedBox.DividedBox#resize}のオプションと同じです
		 */
		show: function(box, opt) {
			// hide状態のボックスを表示
			var $box = this._getBoxElement(box);
			if (!$box.length || !$box.data(DATA_HIDDEN)) {
				return;
			}
			$box.data(DATA_HIDDEN, false);
			// 指定されたindexのボックスの両隣のdividerを表示する
			var $prevDivider = this._getPrevDividerByBox($box);
			var $nextDivider = this._getNextDividerByBox($box);
			if ($prevDivider.length) {
				this.showDivider($prevDivider);
			} else if ($nextDivider.length) {
				this.showDivider($nextDivider, true);
			}

			// コンテンツの大きさにリサイズ
			this.fitToContents(box, opt);
		},

		/**
		 * ボックスを非表示にします
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {index|DOM|jQuery|String} box boxのindexまたはbox要素またはセレクタ
		 * @param {Object} opt {@link h5.ui.components.DividedBox.DividedBox#resize}のオプションと同じです
		 */
		hide: function(box, opt) {
			var $box = this._getBoxElement(box);
			if (!$box.length) {
				return;
			}
			// 指定されたindexの左(上)側ボックスのどちらか片方のdividerを非表示にする
			// 右(下)側にdividerのあるboxの場合、そのdividerは隠されたboxを無視して動作するdividerとして動作する
			// 左(上)端のボックスでdividerが右(下)にしかない場合はそのdividerを非表示にする
			var $prevDivider = this._getPrevDividerByBox($box);
			var $nextDivider = this._getNextDividerByBox($box);
			if ($prevDivider.length) {
				this.hideDivider($prevDivider);
			} else if ($nextDivider) {
				this.hideDivider($nextDivider, true);
			}

			// 0にリサイズ
			this.resize(box, 0, opt);

			$box.data(DATA_HIDDEN, true);
		},

		/**
		 * ボックスのサイズ変更を行います
		 *
		 * @param {index|DOM|jQuery|String} box boxのindexまたはbox要素またはセレクタ
		 * @param {Integer} size リサイズするサイズ
		 * @param {Object} opt リサイズオプション
		 * @param {Number} [opt.partition=0]
		 *            <p>
		 *            左右(上下)にdividerがある場合、resize時に左右(上下)のdividerが動く割合を0.0～1.0を指定します。
		 *            </p>
		 *            <p>
		 *            0.0を指定した場合(デフォルト)は左(上)のdividerを固定して右(下)のdividerの位置が変更されます。
		 *            </p>
		 *            <p>
		 *            左右(上下)のどちらかにしかdividerが無い場合はこのオプションは無視されてresize時に位置が変更されるdividerは自動で決定されます。
		 *            </p>
		 */
		resize: function(box, size, opt) {
			var opt = opt || {};
			var partition = parseFloat(opt.partition) || 0;

			var $targetBox = this._getBoxElement(box);
			var targetOuterSize = this._getOuterSize($targetBox);

			// partitionに合わせて両サイドのdividerを動かす
			// dividerがそもそも片方にしか無い場合はpartitionに関係なくその1つのdividerを動かす
			var $prevDivider = this._getPrevDividerByBox($targetBox);
			var $nextDivider = this._getNextDividerByBox($targetBox);

			if (!$prevDivider.length && !$nextDivider.length) {
				// dividerが無い場合は何もしない
				return;
			}

			if (size == null) {
				// nullの場合はボックス要素のサイズにリサイズする
				// ボックス要素内にスクロールバーが出ている場合、scrollWidth|Heigthを取得してそのサイズにリサイズ
				// StateBoxの場合、コンテンツの中身のサイズにリサイズする

				// FIXME StateBoxが子コントローラだった場合はgetControllers()で取得できないので、data属性を使って取得
				var stateBox = $targetBox.data('h5controller-statebox-instance');
				if (stateBox && stateBox.getContentsSize) {
					// 中身のサイズ(StateBoxのコンテンツのサイズ)を取得
					size = stateBox.getContentsSize()[this._w_h];
				} else {
					// 中身のサイズ(scrollWidth|scrollHeight)を取得
					size = $targetBox[0][this._scrollW_H];
				}
				// 中身のサイズにpadding/border/marginの値を加える
				size += +targetOuterSize - $targetBox[this._w_h]();
			}

			var totalMove = size - targetOuterSize;
			if (!$prevDivider.length) {
				partition = 0;
			} else if (!$nextDivider.length) {
				partition = 1;
			}
			var prevMove = -totalMove * partition;
			var nextMove = totalMove + prevMove;

			var isfixedSize = $targetBox.hasClass(CLASS_FIXED_SIZE);
			// reisze対象のboxがfixedSizeの場合はfixedSize指定を一旦無視してリサイズする
			if (isfixedSize) {
				$targetBox.removeClass(CLASS_FIXED_SIZE);
			}
			if (prevMove) {
				this._move(prevMove, $prevDivider);
			}
			if (nextMove) {
				this._move(nextMove, $nextDivider);
			}

			// 一旦外したfixedSize指定を再設定
			if (isfixedSize) {
				$targetBox.addClass(CLASS_FIXED_SIZE);
			}
		},

		/**
		 * ボックスを今のサイズで固定にする
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox*
		 * @param {index|DOM|jQuery|String} box boxのindexまたはbox要素またはセレクタ
		 */
		fixSize: function(box) {
			var $targetBox = this._getBoxElement(box);
			$targetBox.addClass(CLASS_FIXED_SIZE);
			this.refresh();
		},

		/**
		 * ボックスの固定を解除する
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox*
		 * @param {index|DOM|jQuery|String} box boxのindexまたはbox要素またはセレクタ
		 */
		unfixSize: function(box) {
			var $targetBox = this._getBoxElement(box);
			$targetBox.removeClass(CLASS_FIXED_SIZE);
			this.refresh();
		},

		/**
		 * dividerのトラック操作開始時イベントハンドラ
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param context
		 */
		'> .divider h5trackstart': function(context) {
			var $divider = $(context.event.currentTarget);
			var l_t = this._l_t;
			if ($divider.hasClass(CLASS_FIXED_DIVIDER)) {
				return;
			}
			this._lastPos = $divider.position();

			// 同時に動かす要素。dividerの前後のboxがサイズ固定なら、そのサイズを変えないように同時に動かす
			var $dividerGroup = this._getDividerGroup($divider);
			var $groupFirst = $dividerGroup.eq(0);
			var $groupLast = $dividerGroup.eq($dividerGroup.length - 1);
			var $groupPrev = this._getPrevBoxByDivider($groupFirst);
			var $groupNext = this._getNextBoxByDivider($groupLast);
			var afterWH = 0;
			var beforeWH = 0;
			var isAfter = false;
			var $dividers = $dividerGroup.filter('.divider');
			$dividerGroup.each(this.ownWithOrg(function(element) {
				// dividerまたはbox要素
				if (!isAfter && element === $divider[0]) {
					isAfter = true;
				}
				if (!isAfter) {
					beforeWH += this._getOuterSize(element);
				}
				if (isAfter) {
					afterWH += this._getOuterSize(element);
				}
			}));

			// dividerのトラック操作が開始したことをイベントで通知
			// 同時に動くdividerがある場合でも実際に操作しているdividerからのみ上げる
			// next,prevには実際にサイズが変更されるボックスが上がる
			// (前後にfixedSizeのボックスがある場合、fixedSizeのボックスではなく、サイズが変わるボックスが対象)
			$divider.trigger(EVENT_DIVIDER_TRACK_START, {
				prev: $groupPrev[0],
				next: $groupNext[0],
				$dividers: $dividers
			});

			// 可動範囲をh5trackstartの時点で決定する
			// 両サイドのボックスで最小サイズが指定されている場合は、両サイドが指定されたサイズより小さくならないようにする
			this._trackingData = {
				$dividerGroup: $dividerGroup,
				$dividers: $dividers,
				prevStart: beforeWH + $groupPrev.position()[l_t]
						+ ($groupPrev.data(DATA_MIN_BOX_SIZE) || 0),
				nextEnd: $groupNext.position()[l_t] + this._getOuterSize($groupNext) - afterWH
						- ($groupNext.data(DATA_MIN_BOX_SIZE) || 0),
				$groupPrev: $groupPrev,
				$groupNext: $groupNext
			};
		},

		/**
		 * dividerのトラック操作中イベントハンドラ
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param context
		 */
		'> .divider h5trackmove': function(context) {
			var $divider = $(context.event.currentTarget);
			if ($divider.hasClass(CLASS_FIXED_DIVIDER)) {
				return;
			}
			var event = context.event;
			event.preventDefault();
			var dividerOffset = $divider.offset();
			var type = this._type;
			var move = (type === 'x') ? event.pageX - dividerOffset.left : event.pageY
					- dividerOffset.top;
			if (move === 0) {
				return;
			}
			var trackingData = this._trackingData;
			trackingData.lastPos = $divider.position();
			this._move(move, $divider, this._trackingData);
			$divider.trigger(EVENT_DIVIDER_TRACK_MOVE, {
				prev: trackingData.$groupPrev[0],
				next: trackingData.$groupNext[0],
				$dividers: this._trackingData.$dividers,
				moved: move
			});
		},

		/**
		 * dividerのトラック操作終了イベントハンドラ
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param context
		 */
		'> .divider h5trackend': function(context) {
			var $divider = $(context.event.currentTarget);
			var trackingData = this._trackingData;
			$divider.trigger(EVENT_DIVIDER_TRACK_END, {
				prev: trackingData.$groupPrev[0],
				next: trackingData.$groupNext[0],
				$dividers: this._trackingData.$dividers
			});
			// キャッシュした値のクリア
			this._trackingData = null;
		},

		/**
		 * 指定されたdividerを非表示にする
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {index|DOM|jQuery|String} dividerのindexまたはdivider要素またはセレクタ
		 * @param {Boolean} [forwardAdjust=false]
		 *            dividerを非表示にするとき、divider分の幅をどちらのボックスで埋めるか。左(上)で埋める場合はtrueを指定。
		 */
		hideDivider: function(divider, forwardAdjust) {
			var $divider = this._getDividerElement(divider);
			if ($divider.css('display') === 'none') {
				return;
			}
			var dividerWH = $divider[this._w_h]();
			if (forwardAdjust) {
				var $prev = this._getPrevBoxByDivider($divider);
				this._setOuterSize($prev, this._getOuterSize($prev) + dividerWH);
				$divider.css(this._l_t, '+=' + dividerWH);
			} else {
				var $next = this._getNextBoxByDivider($divider);
				this._setOuterSize($next, this._getOuterSize($next) + dividerWH);
				$next.css(this._l_t, '+=' + dividerWH);
			}
			$divider.css('display', 'none');
			this.refresh();
		},

		/**
		 * 指定されたdividerを表示する
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {index|DOM|jQuery|String} dividerのindexまたはdivider要素またはセレクタ
		 * @param {Boolean} [forwardAdjust=false]
		 *            dividerを表示するとき、divider分の幅をどちらのボックスがずらすか。左(上)をずらす場合はtrueを指定。
		 */
		showDivider: function(divider, forwardAdjust) {
			var $divider = this._getDividerElement(divider);
			if ($divider.css('display') === 'block') {
				return;
			}
			var dividerWH = $divider[this._w_h]();
			if (forwardAdjust) {
				var $prev = this._getPrevBoxByDivider($divider);
				this._setOuterSize($prev, this._getOuterSize($prev) - dividerWH);
				$divider.css(this._l_t, '-=' + dividerWH);
			} else {
				var $next = this._getNextBoxByDivider($divider);
				this._setOuterSize($next, this._getOuterSize($next) - dividerWH);
				$next.css(this._l_t, '-=' + dividerWH);

			}
			$divider.css('display', 'block');
			this.refresh();
		},

		/**
		 * dividerを動かす
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {Integer} move 移動量
		 * @param {Object} trackingData 移動に必要な計算済みデータ。トラック操作の場合は予め計算したデータを使用する
		 * @param {DOM|jQuery} trackingData.divider divider
		 * @param {jQuery} trackingData.$dividerGroup dividerを動かした時に同時に動く要素。
		 *            dividerの前後のboxがfix指定の時に要素のサイズを変えないように同時に動かす必要がある。 (指定しない場合は、_move内で計算)
		 * @param {Integer} trackingData.prevStart 左(上)の移動限界位置(指定しない場合は_move内で計算)
		 * @param {Integer}trackingData.nextStart 右(下)の移動限界位置(指定しない場合は_move内で計算)
		 * @param {Object} trackingData.lastPost 移動前の位置(指定しない場合は_move内で計算)
		 */
		_move: function(move, divider, trackingData) {
			if (move === 0) {
				return;
			}
			var $divider = $(divider);
			var l_t = this._l_t;

			// 負方向への移動時は正方向にあるdividerGroupを探索して同時に動かす
			// 逆に、正方向への移動なら負方向を探索
			var isTrack = !!trackingData;
			var $dividerGroup = isTrack ? trackingData.$dividerGroup : this
					._getDividerGroup($divider);
			var $groupFirst = $dividerGroup.eq(0);
			var $groupLast = $dividerGroup.eq($dividerGroup.length - 1);
			var $groupPrev = this._getPrevBoxByDivider($groupFirst);
			var $groupNext = this._getNextBoxByDivider($groupLast);
			var prevStart, nextEnd, lastPos;
			if (isTrack) {
				prevStart = trackingData.prevStart;
				nextEnd = trackingData.nextEnd;
				lastPos = trackingData.lastPos;
			} else {
				// 第3引数が未指定ならprevStart,nextEnd,lastPosはdividerから計算する
				// (トラック操作の場合はキャッシュしてある値を渡しているので計算する必要はない)
				var isVisibleDivider = $divider.css('display') === 'block';
				if (isVisibleDivider) {
					lastPos = $divider.position();
				} else {
					// 非表示の場合はboxの位置を基にする
					lastPos = this._getNextBoxByDivider($divider).position();
				}
				// 両サイドのボックスの最小サイズを考慮して可動範囲を決定
				prevStart = $groupPrev.length ? $groupPrev.position()[l_t]
						+ ($groupPrev.data(DATA_MIN_BOX_SIZE) || 0) : 0;
				nextEnd = $groupNext.length ? ($groupNext.position()[l_t]
						+ this._getOuterSize($groupNext) - (isVisibleDivider ? this
						._getOuterSize($divider) : 0))
						- ($groupNext.data(DATA_MIN_BOX_SIZE) || 0) : $divider.position()[l_t];
			}
			var moved = lastPos[l_t] + move;
			if (moved <= prevStart + 1) {
				move = prevStart - lastPos[l_t];
				if (move <= -1 && isTrack)
					return;
			} else if (moved >= nextEnd - 1) {
				move = nextEnd - lastPos[l_t];
				if (move >= 1 && isTrack) {
					return;
				}
			}

			var prevWH = this._getOuterSize($groupPrev) + move;
			if (prevWH < 0) {
				prevWH = 0;
				move = -this._getOuterSize($groupPrev);
			}

			var nextWH = this._getOuterSize($groupNext) - move;
			if (nextWH < 0) {
				nextWH = 0;
			}

			$dividerGroup.each(function() {
				$(this).css(l_t, '+=' + move);
			});
			this._setOuterSize($groupPrev, prevWH);
			this._setOuterSize($groupNext, nextWH);
			$groupNext.css(l_t, '+=' + move);
		},

		/**
		 * 現在のdividedBoxのサイズに合わせてボックスのサイズと位置の調整を行う
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 */
		_adjust: function() {
			var l_t = this._l_t;
			var w_h = this._w_h;
			var $root = this._$root;
			var outerW_H = this._outerW_H;
			var adjustAreaWH = $root[w_h]();
			// dividedBoxのサイズの差分
			var divSize = adjustAreaWH - this._lastAdjustAreaWH;
			if (divSize === 0) {
				// サイズに差分が無いなら何もしない
				return;
			}
			this._lastAdjustAreaWH = adjustAreaWH;

			// 各ボックスの割合を保って、ボックスの幅を今の表示幅に合わせる

			// サイズ変更しないボックス(fixedSize)以外のないボックスを拡大・縮小する
			var $boxes = this._getBoxes();
			var $unfixedSizeBoxes = $boxes.not('.' + CLASS_FIXED_SIZE + ',.' + CLASS_KEEP_SIZE);

			var move = 0;
			// サイズが固定でないボックスのトータルサイズ
			var unfixedSizeBoxesTotalSize = 0;
			$unfixedSizeBoxes.each(function() {
				unfixedSizeBoxesTotalSize += $(this)[w_h]();
			});
			$unfixedSizeBoxes.each(this.ownWithOrg(function(box) {
				var $box = $(box);
				if (move) {
					// dividerの移動量に合わせてボックスも移動
					$box.css(l_t, '+=' + move);
				}
				var $divider = this._getNextDividerByBox($box);
				if (!$divider.length) {
					return;
				}
				var isDisplayNone = $divider.css('display') === 'none';
				if (isDisplayNone) {
					$divider.css('display', 'block');
				}
				// 固定ボックス以外のボックスのサイズの比率で拡縮した時の位置にdividerを動かす
				// 固定ボックス以外のボックスのサイズが全て0ならそれらのサイズは等分する
				move += unfixedSizeBoxesTotalSize ? divSize
						* (this._getOuterSize($box) / unfixedSizeBoxesTotalSize) : divSize
						/ $unfixedSizeBoxes.length;

				// グループ化されている(連動して動く)divider全て動かす
				var $group = this._getDividerGroup($divider);
				if (move) {
					$group.css(l_t, '+=' + move);
				}

				if (isDisplayNone) {
					$divider.css('display', 'none');
				}
			}));

			// 各ボックスのサイズ変更とdividerの移動
			$boxes.each(this.ownWithOrg(function(box) {
				var $box = $(box);
				if ($box.data(DATA_HIDDEN)) {
					return;
				}
				var $prev = this._getPrevDividerByBox($box);
				var $next = this._getNextDividerByBox($box);
				var outerSize = 0;
				// 非表示の場合はいったん表示する(位置取得のため)
				var isPrevDisplayNone = $prev.css('display') === 'none';
				var isNextDisplayNone = $next.css('display') === 'none';
				if (isPrevDisplayNone) {
					$prev.css('display', 'block');
				}
				if (isNextDisplayNone) {
					$next.css('display', 'block');
				}

				if (!$prev.length) {
					outerSize = $next.length ? $next.position()[l_t] : adjustAreaWH;
				} else if (!$next.length) {
					outerSize = adjustAreaWH - $prev.position()[l_t]
							- (isPrevDisplayNone ? 0 : this._getOuterSize($prev));
				} else {
					outerSize = $next.position()[l_t] - $prev.position()[l_t]
							- (isPrevDisplayNone ? 0 : this._getOuterSize($prev));
				}
				// 表示にしたdividerを元に戻す
				if (isPrevDisplayNone) {
					$prev.css('display', 'none');
				}
				if (isNextDisplayNone) {
					$next.css('display', 'none');
				}
				// 計算したサイズを設定
				this._setOuterSize($box, outerSize);
			}));
		},

		/**
		 * ボックスのouterWidthまたはouterHeightを取得
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {jQuery} $el
		 * @param {Integer} outerSize
		 */
		_getOuterSize: function(box) {
			// margin,border,paddingの合計値より、設定したouterSizeの値が小さい場合(=設定したouterSizeと実際のouterSizeが異なる場合)、
			// データ属性に設定したouterSizeが格納されているのでそれを返す
			var $box = $(box);
			var virtualSize = $box.data(DATA_VIRTUAL_OUTER_SIZE);
			return virtualSize != null ? virtualSize : $box[this._outerW_H](true);
		},

		/**
		 * ボックスのouterWidthまたはouterHeightがouterSizeになるようにサイズを設定
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {jQuery|DOM} box
		 * @param {Integer} outerSize
		 */
		_setOuterSize: function(box, outerSize) {
			var $box = $(box);
			// outerWidth/Heightとcssで指定するwidht/heightの差
			var outerW_H = this._outerW_H;
			var w_h = this._w_h;
			var pre = parseFloat($box[w_h]());
			var mbp = $box[outerW_H](true) - $box[w_h]();
			var after = (outerSize - mbp);

			if (after < 0) {
				// 設定したいouterSizeが、margin,border,paddingの合計値未満の場合、width|heightの設定値は負の値になる
				// 負の値を設定しても0を指定した場合と変わらない
				// dividedBoxで使用するボックスのサイズは設定されたサイズを使用する必要があるため、
				// データ属性に設定されたouterSizeを持たせる
				$box.data(DATA_VIRTUAL_OUTER_SIZE, outerSize);
				// 設定する値は負の数にしない
				after = 0;
			} else {
				$box.data(DATA_VIRTUAL_OUTER_SIZE, null);
			}

			$box[w_h](after);

			if (pre !== after) {
				// 実際にサイズが変更されたらイベントをあげる
				$box.trigger(EVENT_BOX_SIZE_CHANGE, {
					oldValue: pre,
					newValue: after
				});
			}
		},

		/**
		 * エレメント内のホワイトスペース(===空のTEXT_NODE)を削除
		 * <p>
		 * prototype.js v1.5.0のElement.cleanWhitespace()相当のことを行っている
		 * </p>
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {DOM} element
		 */
		_cleanWhitespace: function(element) {
			var node = element.firstChild;
			while (node) {
				var nextNode = node.nextSibling;
				if (node.nodeType === 3 && !/\S/.test(node.nodeValue))
					element.removeChild(node);
				node = nextNode;
			}
			return element;
		},

		/**
		 * 全てのボックスを取得
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @returns {jQuery}
		 */
		_getBoxes: function() {
			// ルート要素直下の要素(divider以外)
			// ただし、動的に追加された要素でかつposition:absoluteのものは除く
			// (動的に追加された要素でもposition:absoluteでなければ新規boxとして位置計算の対象にする
			return this.$find('> :not(.divider)').filter(function() {
				return $(this).hasClass(CLASS_MANAGED) || $(this).css('position') !== 'absolute';
			});
		},

		/**
		 * 全てのdividerを取得
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @returns {jQuery}
		 */
		_getDividers: function() {
			return this.$find('>.divider');
		},

		/**
		 * indexからdividerを返す。DOM,jQueryが渡された場合はdivider要素ならそれを$()でラップして返す
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {index|DOM|jQuery|String} dividerのindexまたはdivider要素またはセレクタ
		 * @returns {jQuery} divider要素。該当するものが無い場合は空jQuery
		 */
		_getDividerElement: function(divider) {
			var $dividers = this._getDividers();
			if (typeof divider === 'number') {
				return $dividers.eq(divider);
			}
			return $dividers.filter(divider).eq(0);
		},


		/**
		 * indexからボックスを返す。DOM,jQueryが渡された場合はdivider要素ならそれを$()でラップして返す
		 *
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @param {index|DOM|jQuery|String} ボックスのindexまたはボックス要素またはセレクタ
		 * @returns {jQuery}
		 */
		_getBoxElement: function(box) {
			var $boxes = this._getBoxes();
			if (typeof box === 'number') {
				return $boxes.eq(box);
			}
			return $boxes.filter(box).eq(0);
		},

		/**
		 * 指定されたdividerの前のボックスを返す
		 * <p>
		 * ただし非表示のボックスは除いてその前のボックスを返す
		 * </p>
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @returns {jQuery}
		 */
		_getPrevBoxByDivider: function(divider) {
			var $divider = $(divider);
			var $box = $divider.prevAll('.' + CLASS_MANAGED + ':first');
			// hidden状態ならその前のboxを返す。
			// 無い場合は空のjQueryオブジェクトを返す
			if ($box.length && $box.data(DATA_HIDDEN)) {
				return this._getPrevBoxByDivider($box.prev());
			}
			return $box;
		},

		/**
		 * 指定されたdividerの次のボックスを返す
		 * <p>
		 * ただし非表示のボックスは除く
		 * </p>
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @returns {jQuery}
		 */
		_getNextBoxByDivider: function(divider) {
			var $divider = $(divider);
			var $box = $divider.nextAll('.' + CLASS_MANAGED + ':first');
			// hidden状態ならその次のboxを返す。
			// 無い場合は空のjQueryオブジェクトを返す
			if ($box.length && $box.data(DATA_HIDDEN)) {
				return this._getNextBoxByDivider($box.next());
			}
			return $box;
		},

		/**
		 * 指定されたボックスの前のdividerを返す
		 * <p>
		 * ただし非表示のdividerは除いてその前のdividerを返す
		 * </p>
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @returns {jQuery}
		 */
		_getPrevDividerByBox: function(box) {
			var $box = $(box);
			var $divider = $box.prevAll('.divider:first');
			// dividerの前にボックスがない(先頭要素)、またはdividerの前のボックスがhiddenなら
			// dividerは無効なので空jQueryを返す
			if ($divider.length
					&& (!$divider.prevAll('.' + CLASS_MANAGED).length || $divider.prevAll(
							'.' + CLASS_MANAGED + ':first').data(DATA_HIDDEN))) {
				return $();
			}
			return $divider;
		},

		/**
		 * 指定されたボックスの次のdividerを返す
		 * <p>
		 * ただし非表示のdividerは除いてその次のdividerを返す
		 * </p>
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 * @returns {jQuery}
		 */
		_getNextDividerByBox: function(box) {
			var $box = $(box);
			var $divider = $box.nextAll('.divider:first');
			// 次のボックスがhiddenならその次のdividerを返す
			if ($divider.length && $divider.next().data(DATA_HIDDEN)) {
				return this._getNextDividerByBox($divider.next());
			}
			return $divider;
		},

		/**
		 * dividerを動かす時にそのdividerと連動して動く要素(divider,box)をjQueryオブジェクトで返す
		 *
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 */
		_getDividerGroup: function($divider) {
			var $result = $divider;
			var $d = $divider;
			var $b = this._getNextBoxByDivider($d);
			while (true) {
				if (!$b.hasClass(CLASS_FIXED_SIZE) && !$b.hasClass(CLASS_KEEP_SIZE)) {
					break;
				}
				$result = $result.add($b);
				$d = this._getNextDividerByBox($b);
				$result = $result.add($d);
				$b = this._getNextBoxByDivider($d);
			}
			$d = $divider;
			$b = this._getPrevBoxByDivider($d);
			while (true) {
				if (!$b.hasClass(CLASS_FIXED_SIZE) && !$b.hasClass(CLASS_KEEP_SIZE)) {
					break;
				}
				$result = $result.add($b);
				$d = this._getPrevDividerByBox($b);
				$result = $result.add($d);
				$b = this._getPrevBoxByDivider($d);
			}
			return $result;
		},

		/**
		 * @private
		 * @memberOf h5.ui.components.DividedBox.DividedBox
		 */
		__unbind: function() {
			var $root = this._$root = $(this.rootElement);
			$root.removeClass(CLASS_ROOT);
			$root.attr('style', '');
			this.$find('.divider').remove();
			var $boxes = this.$find('.' + CLASS_MANAGED);
			$boxes.removeClass(CLASS_MANAGED);
			$boxes.attr('style', '');
		}
	};

	h5.core.expose(dividedBoxController);
})();
