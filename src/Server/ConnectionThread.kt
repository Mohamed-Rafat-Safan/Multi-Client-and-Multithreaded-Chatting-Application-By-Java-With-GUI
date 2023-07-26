package Server

import Repository.Repository
import java.io.*
import java.net.Socket

/**
 *
 * @author El-Mancy
 */
class ConnectionThread(private val socket: Socket, private var repository: Repository?) : Runnable {
    private val outputStream: OutputStream
    private val inputStream: InputStream
    private var i: Int

    init {
        this.repository = repository
        outputStream = socket.getOutputStream()
        inputStream = socket.getInputStream()
        i = 0
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    override fun run() {
        try {
            handleConnection()
        } catch (e: Exception) {
            println(e)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    @Throws(Exception::class)
    private fun handleConnection() {
        println("Client is Connected Successfully")
        val outputStreamWriter = OutputStreamWriter(outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        while (true) {
            val response = bufferedReader.readLine()
            if (response != null) {
                val responseArray: Array<String?> = response.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                var Response: String
                when (responseArray[0]) {

                    //////////////////////////////////////////////////////////////////////////////////

                    "Login" -> {
                        Response = repository!!.Login(
                            socket, responseArray[1],
                            responseArray[2]
                        ).toString()
                        bufferedWriter.write(
                            """
                                $Response
                                
                                """.trimIndent()
                        )
                        bufferedWriter.flush()
                    }

                    //////////////////////////////////////////////////////////////////////////////////

                    "Register" -> {
                        Response = repository!!.Register(
                            responseArray[1],
                            responseArray[2], responseArray[3]
                        ).toString()
                        bufferedWriter.write(
                            """
                                $Response
                                
                                """.trimIndent()
                        )
                        bufferedWriter.flush()
                    }

                    //////////////////////////////////////////////////////////////////////////////////

                    "Friend Request" -> {
                        val socket1: Socket? = repository?.Socket(responseArray[2])
                        if (socket1 == null) {
                            bufferedWriter.write(
                                """
                                    UserName NotFound:${responseArray[2]}
                                    
                                    """.trimIndent()
                            )
                            bufferedWriter.flush()
                        } else {
                            val outputStream1 = socket1.getOutputStream()
                            val outputStreamWriter1 = OutputStreamWriter(outputStream1)
                            val bufferedWriter1 = BufferedWriter(outputStreamWriter1)
                            bufferedWriter1.write(
                                """
                                    Friend Request:${responseArray[1]}:${responseArray[2]}
                                    
                                    """.trimIndent()
                            )
                            bufferedWriter1.flush()
                        }
                    }

                    //////////////////////////////////////////////////////////////////////////////////

                    "Accept Request" -> {
                        repository!!.addChattingList(responseArray[1] + ":" + responseArray[2])
                        val socket2: Socket = repository!!.Socket(responseArray[2])!!
                        val outputStream1 = socket2.getOutputStream()
                        val outputStreamWriter1 = OutputStreamWriter(outputStream1)
                        val bufferedWriter1 = BufferedWriter(outputStreamWriter1)
                        bufferedWriter1.write(
                            """
                                Accept Request:${responseArray[1]}:${responseArray[2]}
                                
                                """.trimIndent()
                        )
                        bufferedWriter1.flush()
                    }

                    //////////////////////////////////////////////////////////////////////////////////

                    "Private Message" -> if (repository!!.checkInappropriateWords(responseArray[3])) {
                        i++
                        bufferedWriter.write(
                            """
                                Blocked:${responseArray[2]}
                                
                                """.trimIndent()
                        )
                        bufferedWriter.flush()
                    } else {
                        val socket3: Socket? = repository!!.Socket(responseArray[2])
                        val outputStream2 = socket3?.getOutputStream()
                        val outputStreamWriter2 = OutputStreamWriter(outputStream2)
                        val bufferedWriter2 = BufferedWriter(outputStreamWriter2)
                        bufferedWriter2.write(
                            """
                                Private Message:${responseArray[1]}:${responseArray[3]}
                                
                                """.trimIndent()
                        )
                        bufferedWriter2.flush()
                    }

                    //////////////////////////////////////////////////////////////////////////////////

                    "Group Message" -> if (repository!!.checkInappropriateWords(responseArray[responseArray.size - 1])) {
                        i++
                        var chatName: String? = ""
                        var x = 2
                        while (x < responseArray.size - 1) {
                            chatName += if (x == responseArray.size - 2) {
                                responseArray[x]
                            } else {
                                responseArray[x] + ":"
                            }
                            x++
                        }
                        bufferedWriter.write("Blocked:$chatName\r\n")
                        bufferedWriter.flush()
                    } else {
                        run {
                            var i = 2
                            while (i < responseArray.size - 1) {
                                val ToSocket3: Socket? =
                                    repository!!.Socket(responseArray[i])
                                val outputStream1 = ToSocket3!!.getOutputStream()
                                val outputStreamWriter1 = OutputStreamWriter(outputStream1)
                                val bufferedWriter1 = BufferedWriter(outputStreamWriter1)
                                var chatname = ""
                                val list =
                                    DeleteElemet(responseArray, i)
                                var x1 = 1
                                while (x1 < list!!.size - 1) {
                                    chatname += if (x1 == list.size - 2) {
                                        list[x1]
                                    } else {
                                        list[x1] + ":"
                                    }
                                    x1++
                                }
                                bufferedWriter1.write(
                                    """
                                        Group Message:${responseArray[1]}:$chatname:${responseArray[responseArray.size - 1]}
                                        
                                        """.trimIndent()
                                )
                                bufferedWriter1.flush()
                                i++
                            }
                        }
                    }

                    //////////////////////////////////////////////////////////////////////////////////

                    "Join Chat" -> {
                        var name: String? = ""
                        var x = 2
                        while (x < responseArray.size) {
                            name += if (x == responseArray.size - 1) {
                                responseArray[x]
                            } else {
                                responseArray[x] + ":"
                            }
                            x++
                        }
                        if (repository!!.joinChat(responseArray[1], name)) {
                            bufferedWriter.write("GroupName Founded:$name\r\n")
                            bufferedWriter.flush()
                            var x = 2
                            while (x < responseArray.size) {
                                var chatname1 = ""
                                val socket5: Socket? = repository!!.Socket(responseArray[x])
                                val outputStream5 = socket5!!.getOutputStream()
                                val streamWriter5 = OutputStreamWriter(outputStream5)
                                val bw5 = BufferedWriter(streamWriter5)
                                val list2 = DeleteElemet(responseArray, x)
                                var x1 = 2
                                while (x1 < list2!!.size) {
                                    chatname1 += if (x1 == list2.size - 1) {
                                        list2[x1]
                                    } else {
                                        list2[x1] + ":"
                                    }
                                    x1++
                                }
                                bw5.write(
                                    """
                                        Join Chat:${responseArray[1]}:$chatname1
                                        
                                        """.trimIndent()
                                )
                                bw5.flush()
                                x++
                            }
                        } else {
                            bufferedWriter.write(
                                """
                                    GroupName NotFound
                                    
                                    """.trimIndent()
                            )
                            bufferedWriter.flush()
                        }
                    }

                    else -> {}
                }
            }
        }
    }



        /////////////////////////////////////////////////////////////////////////////////////////
        fun DeleteElemet(arr: Array<String?>?, index: Int): Array<String?>? {
            if (arr == null || index < 0 || index >= arr.size) {
                return arr
            }
            val anotherArray = arrayOfNulls<String>(arr.size - 1)
            run {
                var i = 0
                var k = 0
                while (i < arr.size) {
                    if (i == index) {
                        i++
                        continue
                    }
                    anotherArray[k++] = arr.get(i)
                    i++
                }
            }
            return anotherArray
        }

}