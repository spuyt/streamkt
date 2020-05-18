package streamkt.util

import kotlin.js.Date

actual fun unix():Long{
   return Date.now().toLong()
}