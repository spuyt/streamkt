package io.spuyt.streamkt.event

import java.security.SecureRandom
import java.util.*

// id format:
//---------------------------
// timestamp milliseconds: Long
//      for readability the timestamp is displayed as seconds with the higher precision part being
//      displayed after the dot
// iterator: uint16 hex (for fixed length, makes it sortable)
// random 160 bit (5 bytes) for global uniqueness without any priors encoded as base64 string
//
// size:
// 64 - 16 - 160 = 240 bits in total, in non-human readable format
//
// human readable size:
// 1589187064.737-ffff-VGhlIHF1aWNrIGJyb3duIGZveCA=
//                  48 characters (all 8 bit in utf-8) so 48 bytes or 384 bits
//
// the last 28 characters are the device id, you save this on the device
// this way you can have fewer chances of collision
//
// With a 64bit device-id there would be a 1 in a million chance of collision of ids (not good)
// with 6.07 app installs, even when the id is only generated once and then saved
// so this would give a real realistic chance of collision.
// With a 160bit device-id this one in a million chance would be after 1.71*10^21 installs,
// or 1.71*10^9 trillion device installs. So this is a safer option.


object Gen {
    // init secure random
    // (as opposed to creating random with a seed of the current unix time, because then you might
    // as well just use the current unix time as the random 160 bit id, because it will always
    // generate the same initial values)
    private var rand: SecureRandom = SecureRandom()

    // generate a random device id
    var deviceIdBytes: ByteArray = randBytes() // init to random value
    private var deviceId: String = Base64.getEncoder().encodeToString(deviceIdBytes)
        get() = Base64.getEncoder().encodeToString(deviceIdBytes)

    private var unixMs: Long = System.currentTimeMillis()
    private var iter: UShort = 0U // increases with every event that happens at the same millisecond, to prevent duplicate ids

    fun nextGUID(): String {
        var id = ""
        synchronized(this) {
            // see if we need to update the iterator for sub-millisecond precision
            val ms = System.currentTimeMillis()
            if(ms != unixMs) {
                unixMs = ms
                iter = 0U
            } else {
                iter++
            }
            // display time formatted as seconds for readability: 1589187064.737
            val unixSec: Double = unixMs.toDouble() / 1000
            val unixSecStr: String = "%.3f".format(unixSec)
            // encode the iterator as a hex value to make it more compact and fix the size
            val iterStr: String = "%.4x".format(iter)

            // generate the id, should come out as:
            // 1589187064.737-ffff-VGhlIHF1aWNrIGJyb3duIGZveCA=
            id = unixSecStr + "-" + iterStr + "-" + deviceId
        }
        return id
    }

    private fun randBytes(n: Int = 160/8): ByteArray {
        val bs = ByteArray(n)
        rand.nextBytes(bs)
        return bs
    }
}
