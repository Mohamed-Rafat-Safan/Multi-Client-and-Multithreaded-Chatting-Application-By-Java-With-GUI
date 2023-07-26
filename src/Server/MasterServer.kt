package Server.Server

import Repository.Repository
import Server.ConnectionThread
import java.net.ServerSocket

/**
 *
 * @author El-Mancy
 */
object MasterServer {
    var repository: Repository? = null
    @Throws(Exception::class)
    @JvmStatic
    fun main(argv: Array<String>) {
        repository = Repository()
        val serverSocket = ServerSocket(1234)
        println("Server is Running on port 1234")
        while (true) {
           var socket = serverSocket.accept()
            val request = ConnectionThread(socket, repository)
            val thread: Thread = Thread(request)
            thread.start()
        }
    }
}