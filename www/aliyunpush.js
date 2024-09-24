var exec = require('cordova/exec');

var AliyunPush = {

  /**
   * 阿里云推送消息事件，需要重写函数
   * @return {void}
   */
  onMessage: function (message) {
    console.log('AliyunPush Message: ' + message);
  },

  /**
   * 阿里云推送错误事件，需要重写函数
   * @return {void}
   */
  onError: function (error) {
    console.log('AliyunPush Error: ' + error);
  },


  /**
   * 启动推送
   * @return {void}
   */
  boot: async function () {
    this._bindNative('onMessage', this.onMessage);
    return await this._callNative('boot', []);
  },

  /**
   * 检查通知的权限
   * @param  {boolean} force 检查到没权限时直接申请权限
   * @return {object} { granted: true, asked: 5 }
   */
  checkPermission: async function (force) {
   return await this._callNative('checkPermission', [force]);
  },

  /**
   * 打开App设置页
   * @return {void}
   */
  openAppSettings: async function () {
    return await this._callNative('openAppSettings', []);
  },

  /**
   * 没有权限时，请求开通通知权限
   * @return {void}
   */
  requestPermission: async function () {
    return await this._callNative('requestPermission', []);
  },

  /**
   * 获取设备唯一标识deviceId，deviceId为阿里云移动推送过程中对设备的唯一标识（并不是设备UUID/UDID）
   * @return {string} 设备注册码
   */
  getRegisterId: async function () {
   return await this._callNative('getRegisterId', []);
  },

  /**
   * 阿里云推送绑定账号名
   * @param  {string} account 账号
   * @return {void}
   */
  bindAccount: async function (account) {
    return await this._callNative('bindAccount', [account]);
  },

  /**
   * 阿里云推送解除账号名,退出切换账号时调用
   * @return {void}
   */
  unbindAccount: async function () {
    return await this._callNative('unbindAccount', []);
  },

  /**
   * 阿里云推送绑定标签
   * @param  {string} target 目标
   * @param  {string[]} tags 标签列表
   * @param  {string} alias 别名
   * @return {void}
   */
  bindTags: async function (target, tags, alias) {
    return await this._callNative('bindTags', [target, tags, alias]);
  },

  /**
   * 阿里云推送解除绑定标签
   * @param  {string} target 目标
   * @param  {string[]} tags 标签列表
   * @param  {string} alias 别名
   * @return {void}
   */
  unbindTags: async function (target, tags, alias) {
    return await this._callNative('unbindTags', [target, tags, alias]); 
  },

  /**
   * 阿里云推送列出标签
   * @return {void}
   */
  listTags: async function () {
    return await this._callNative('listTags', []);
  },

  /**
   * 添加别名
   * @param  {string} alias 别名
   * @return {void}
   */
  addAlias: async function (alias) {
    return await this._callNative('addAlias', [alias]);
  },

  /**
   * 解绑别名
   * @param  {string|null} alias 别名, null解绑所有别名
   * @return {void}
   */
  removeAlias: async function (alias) {
    return await this._callNative('removeAlias', [alias]);
  },

  /**
   * 获取别名列表
   * @return {void}
   */
  listAliases: async function () {
    return await this._callNative('listAliases', []);
  },

  /**
   * 设置服务端角标数量 - iOS ONLY
   * @param  {string} badgeNum 角标数量
   * @return {void}
   */
  syncBadgeNum: async function (badgeNum) {
    return await this._callNative('syncBadgeNum', [badgeNum]);
  },

  /**
   * 设置本地角标数量 - iOS ONLY
   * @param  {string} badgeNum 角标数量
   * @return {void}
   */
  setBadgeNum: async function (badgeNum) {
    return await this._callNative('setBadgeNum', [badgeNum]);
  },

  /**
   * 桥接native函数
   * @param  {string} name native中的函数名
   * @param  {*[]} args native中的函数参数
   * @return {string|object} 成功：返回空字符 "" | 错误：抛出错误 { reason, message }
   */
  _callNative: function (name, args) {
    return new Promise((resolve, reject) => exec(data => resolve(data), err => reject(err), 'AliyunPush', name, args));
  },

    /**
   * 桥接native事件
   * @param  {string} name native中的函数名
   * @param  {function} handler 处理事件的函数 
   * @return {void}
   */
  _bindNative: function (name, handler) {
    exec(handler, this.onError, 'AliyunPush', name, []);
  },

};
module.exports = AliyunPush;
