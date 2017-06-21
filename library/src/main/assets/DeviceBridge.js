const CALLBACK_PROPERTY_NAME = 'callBackId'
const CALLBACK_RESPONSE_PROPERTY_NAME = 'responseId'
const HANDLER_PROPERTY_NAME = 'handlerName'
const PROTOCOL_NAME = 'JsBridge'
const MESSAGE_HOST_NAME = 'message'
const MESSAGE_PARAM_NAME = 'msg'
var uniqueId = 1
const callBacks = {}
const messageHandlers = {}

function sendMessage(message, callBack) {
  if (!message || !message[HANDLER_PROPERTY_NAME]) {
    return
  }

  if (callBack) {
    const callBackId = 'callBack_' + uniqueId++ + '_' + message[HANDLER_PROPERTY_NAME]
    callBacks[callBackId] = callBack
    message[CALLBACK_PROPERTY_NAME] = callBackId
  }

  const result = _execDeviceMethod(message)

  return result
}

function onMessageCallBack(responseMessage) {
  const responseId = responseMessage[CALLBACK_RESPONSE_PROPERTY_NAME]
  const responseCallBack = callBacks[responseId]

  if (!responseCallBack) {
    return
  }

  responseCallBack(responseMessage)
  delete callBacks[responseId]
}

function receiveMessage(messageJson) {
  const message = JSON.parse(messageJson)
  if (!message) {
    return
  }

  if(message[CALLBACK_RESPONSE_PROPERTY_NAME]){
    onMessageCallBack(message)
    return
  }


  var responseCallback
  if (message[CALLBACK_PROPERTY_NAME]) {
    var callbackId = message[CALLBACK_PROPERTY_NAME]
    responseCallback = function (responseMessage) {
      responseMessage[CALLBACK_RESPONSE_PROPERTY_NAME] = callbackId
      sendMessage(responseMessage)
    }
  }

  var handler
  if (message[HANDLER_PROPERTY_NAME]) {
    handler = messageHandlers[message[HANDLER_PROPERTY_NAME]]
  }

  if (handler) {
    handler(message, responseCallback)
  } else {
    console.log('未找到对应的js处理' + messageJson)
  }
}

function registMessageHandler(handlerName, handler) {
  if (!handlerName || !handler) {
    return false
  }
  messageHandlers[handlerName] = handler
  return true
}
function unRegistMessageHandler(handlerName, handler) {
  if (!handlerName || !handler) {
    return false
  }
  if (!messageHandlers[handlerName]) {
    delete messageHandlers[handlerName]
    return true
  }

  return false
}

function _execDeviceMethod(message) {
 return prompt(PROTOCOL_NAME + '://' + MESSAGE_HOST_NAME + '?' + MESSAGE_PARAM_NAME + '=' + JSON.stringify(message))
}


const DeviceJsBridge = window.DeviceJsBridge = {
  sendMessage: sendMessage,
  onMessageCallBack: onMessageCallBack,
  receiveMessage: receiveMessage,
  registMessageHandler: registMessageHandler,
  unRegistMessageHandler: unRegistMessageHandler
}
