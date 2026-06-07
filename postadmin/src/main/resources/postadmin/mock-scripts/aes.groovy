import com.fadeway32.crypto.util.PostcryptionUtils

def objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
def apiUrl = 'http://127.0.0.1:8088/postadmin/postcryption/mock/aes'
def plaintext = 'hello-aes'

def encrypted = PostcryptionUtils.aesEncrypt(plaintext)
def requestMap = [
        plaintext : plaintext,
        key       : encrypted.key,
        iv        : encrypted.iv,
        ciphertext: encrypted.ciphertext
]

def requestJson = objectMapper.writeValueAsString(requestMap)
def response = com.fadeway32.postapi.util.PostApiUtils.postJson(apiUrl, requestJson)
// Map 入参调用示例:
// def response = com.fadeway32.postapi.util.PostApiUtils.postJson(apiUrl, requestMap)

return [
        url        : apiUrl,
        requestType: 'String',
        request    : requestMap,
        requestJson: requestJson,
        response   : response
]
