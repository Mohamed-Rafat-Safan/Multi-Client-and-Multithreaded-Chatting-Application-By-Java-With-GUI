package Repository

import java.net.Socket

interface Repo {
    fun Login(clientsocket: Socket?, UserName: String?, Password: String?): String?
    fun Register(Name: String?, UserName: String?, Password: String?): String?
    fun Socket(UserName: Socket?): Socket?
    fun addChattingList(chatname: String?)
    fun joinChat(member: String?, chatName: String?): Boolean
    fun checkInappropriateWords(content: String?): Boolean
}