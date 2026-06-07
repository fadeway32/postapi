import com.fadeway32.crypto.util.PostcryptionUtils

return [
        loaded : true,
        message: 'postcryption classes are available in the Groovy runtime classpath',
        md5   : PostcryptionUtils.md5Hex('abc')
]
