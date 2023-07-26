package Repository

import java.awt.Color
import java.awt.Container
import java.net.Socket
import java.util.regex.Pattern
import javax.swing.DefaultListModel
import javax.swing.JFrame
import javax.swing.JList

/**
 *
 * @author El-Mancy
 */
class Repository : Repo {
    private val registeredUsers = ArrayList<Any?>()
    private val loggedinUsers = ArrayList<Any?>()
    private val chattingList = ArrayList<String>()
    private val InappropriateWords = ArrayList<String>()
    private val defaultListModel = DefaultListModel<String>()
    private val jList: JList<String> = JList<String>(defaultListModel)
    private val jFrame = JFrame()

    init {
         jFrame.setTitle("Active User Chat Names On Servers")
         jList.setBounds(0, 30, 500, 500)
         jFrame.add( jList)
         jFrame.setSize(500, 500)
        val contentPane: Container =  jFrame.getContentPane()
        contentPane.background = Color.LIGHT_GRAY
         jList.setBackground(Color.LIGHT_GRAY)
         jFrame.setLayout(null)
         jFrame.setVisible(true)
         InappropriateWords.add("BAD")
         InappropriateWords.add("TERROR")
         InappropriateWords.add("HORROR")
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    override fun Login(clientsocket: Socket?, UserName: String?, Password: String?): String? {
        for (i in  registeredUsers.indices) {
            var objects = arrayOfNulls<Any>(3)
            objects = registeredUsers.get(i) as Array<Any?>
            val username = objects[1] as String?
            val password = objects[2] as String?
            if (password != Password && username == UserName) {
                return "Not Loggedin" + ":" + "401 Error Password is Invalid"
            } else if (password == Password && username == UserName) {
                val Name = objects[0] as String?
                val objects1 = arrayOfNulls<Any>(2)
                objects1[0] = UserName
                objects1[1] = clientsocket
                 loggedinUsers.add(objects1)
                println("$UserName loggedin")
                return "Loggedin:$UserName"
            }
        }
        return "Not Loggedin" + ":" + "404 Error Account is not found"
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    override fun Register(Name: String?, UserName: String?, Password: String?): String? {
        val objects = arrayOfNulls<Any>(3)
        objects[0] = Name
        objects[1] = UserName
        objects[2] = Password
        for (i in  registeredUsers.indices) {
            var objects1 = arrayOfNulls<Any>(3)
            objects1 = registeredUsers.get(i) as Array<Any?>
            val username = objects1[1] as String?
            if (UserName == username) {
                return "Not Registered:"
            }
        }
         registeredUsers.add(objects)
        return "Registered:"
    }

    override fun Socket(UserName: Socket?): Socket? {
        return Socket(UserName)
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    fun Socket(UserName: String?): Socket? {
        for (x in  loggedinUsers.indices) {
            var objects = arrayOfNulls<Any>(2)
            objects = loggedinUsers.get(x) as Array<Any?>
            val userName = objects[0] as String?
            val socket = objects[1] as Socket?
            if (userName == UserName) {
                return socket
            }
        }
        return null
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    override fun addChattingList(chatname: String?) {
        if (chatname != null) {
            chattingList.add(chatname)
        }
         defaultListModel.addElement(chatname)
         jList.setModel( defaultListModel)
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    override fun joinChat(member: String?, chatName: String?): Boolean {
        val newChat = "$chatName:$member"
        var i: Int =  chattingList.indexOf(chatName)
        if (i < 0) {
            return false
        }
         chattingList.set(i, newChat)
        i =  defaultListModel.indexOf(chatName)
         defaultListModel.set(i, newChat)
         jList.setModel( defaultListModel)
        return true
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    override fun checkInappropriateWords(content: String?): Boolean {
        for (i in InappropriateWords.indices) {
            val pattern =
                Pattern.compile(InappropriateWords.get(i), Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(content)
            val matchFound = matcher.find()
            if (matchFound) {
                return true
            }
        }
        return false
    }


    
}