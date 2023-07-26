package Clients

import java.awt.Color
import java.awt.Font
import java.awt.event.*
import java.io.*
import java.net.Socket
import java.util.*
import javax.swing.*

/**
 *
 * @author El-Mancy
 */
object Clients {
    private val JTextField: String? = null
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var userName: String? = null
    private val loginJFrame = JFrame()
    private val userFrame = JFrame()
    private val registerFrame = JFrame()
    private val defaultListModel = DefaultListModel<String>()
    private val jList = JList(defaultListModel)
    private val chattingList = ArrayList<Any>()
    @Throws(Exception::class)
    @JvmStatic
    fun main(argv: Array<String>) {
        socket = Socket("localhost", 1234)
        loginJFrame.title = "Login"
        val contentPane = loginJFrame.contentPane
        contentPane.background = Color.LIGHT_GRAY
        val contentPane2 = userFrame.contentPane
        contentPane2.background = Color.LIGHT_GRAY
        val contentPane3 = registerFrame.contentPane
        contentPane3.background = Color.LIGHT_GRAY
        registerFrame.title = "Register"
        outputStream = socket!!.getOutputStream()
        inputStream = socket!!.getInputStream()
        loginPage()
        while (true) {
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val Response = bufferedReader.readLine()
            val ResponseArray = Response.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            when (ResponseArray[0]) {
                //////////////////////////////////////////////////////////////////////////////////
                "Not Loggedin" -> JOptionPane.showMessageDialog(
                    loginJFrame,
                    ResponseArray[1]
                )
                //////////////////////////////////////////////////////////////////////////////////

                "Loggedin" -> {
                    userName = ResponseArray[1]
                    userPage(userName)
                    loginJFrame.dispose()
                }
                //////////////////////////////////////////////////////////////////////////////////

                "Registered" -> registerFrame.dispose()
                //////////////////////////////////////////////////////////////////////////////////

                "Not Registered" -> JOptionPane.showMessageDialog(registerFrame, "User Name Is Used")
                //////////////////////////////////////////////////////////////////////////////////

                "Friend Request" -> {
                    val res = JOptionPane.showConfirmDialog(userFrame, ResponseArray[1] + "Sent You Friend Request")
                    if (res == JOptionPane.YES_OPTION) {
                        acceptReq(ResponseArray[1])
                        defaultListModel.addElement(ResponseArray[1])
                        jList.model = defaultListModel
                    }
                    chatPage(ResponseArray[1])
                }
                //////////////////////////////////////////////////////////////////////////////////

                "Accept Request" -> {
                    defaultListModel.addElement(ResponseArray[1])
                    jList.model = defaultListModel
                    chatPage(ResponseArray[1])
                }
                //////////////////////////////////////////////////////////////////////////////////

                "UserName NotFound" -> JOptionPane.showMessageDialog(userFrame, ResponseArray[1] + ": Does Not Exsit")
                //////////////////////////////////////////////////////////////////////////////////

                "Join Chat" -> {
                    var chatName = ""
                    var i = 2
                    while (i < ResponseArray.size) {
                        chatName += if (i == ResponseArray.size - 1) {
                            ResponseArray[i]
                        } else {
                            ResponseArray[i] + ":"
                        }
                        i++
                    }
                    val newChat = chatName + ":" + ResponseArray[1]
                    val index = defaultListModel.indexOf(chatName)
                    defaultListModel[index] = newChat
                    jList.model = defaultListModel
                    updateChatName(chatName, newChat)
                }
                //////////////////////////////////////////////////////////////////////////////////

                "GroupName NotFound" -> JOptionPane.showMessageDialog(userFrame, "Incorrect Group Name")
                //////////////////////////////////////////////////////////////////////////////////

                "GroupName Founded" -> {
                    var groupName = ""
                    var x = 1
                    while (x < ResponseArray.size) {
                        groupName += if (x == ResponseArray.size - 1) {
                            ResponseArray[x]
                        } else {
                            ResponseArray[x] + ":"
                        }
                        x++
                    }
                    defaultListModel.addElement(groupName)
                    jList.model = defaultListModel
                    chatPage(groupName)
                }
                //////////////////////////////////////////////////////////////////////////////////

                "Private Message" -> {
                    var i = 0
                    while (i < chattingList.size) {
                        var obj = arrayOfNulls<Any>(3)
                        obj = chattingList[i] as Array<Any?>
                        if (ResponseArray[1] == obj[0]) {
                            if (obj[2] != "Blocked") {
                                (obj[1] as JTextArea?)!!.append(
                                    """
                                        ${ResponseArray[1]}: ${ResponseArray[2]}
                                        
                                        """.trimIndent()
                                )
                            }
                        }
                        i++
                    }
                }
                //////////////////////////////////////////////////////////////////////////////////

                "Group Message" -> {
                    var group = ""
                    run {
                        var x = 2
                        while (x < ResponseArray.size - 1) {
                            group += if (x == ResponseArray.size - 2) {
                                ResponseArray[x]
                            } else {
                                ResponseArray[x] + ":"
                            }
                            x++
                        }
                    }
                    val chars = group.toCharArray()
                    Arrays.sort(chars)
                    var i = 0
                    while (i < chattingList.size) {
                        var arrayOfAnys = arrayOfNulls<Any>(3)
                        arrayOfAnys = chattingList[i] as Array<Any?>
                        val c1 = (arrayOfAnys[0] as String?)!!.toCharArray()
                        Arrays.sort(c1)
                        if (Arrays.equals(chars, c1)) {
                            if (arrayOfAnys[2] != "Blocked") {
                                (arrayOfAnys[1] as JTextArea?)!!.append(
                                    """
                                        ${ResponseArray[1]}: ${ResponseArray[ResponseArray.size - 1]}
                                        
                                        """.trimIndent()
                                )
                            }
                        }
                        i++
                    }
                }
                //////////////////////////////////////////////////////////////////////////////////

                "Blocked" -> {
                    var blocked = ""
                    var i = 1
                    while (i < ResponseArray.size) {
                        blocked += if (i == ResponseArray.size - 1) {
                            ResponseArray[i]
                        } else {
                            ResponseArray[i] + ":"
                        }
                        i++
                    }
                    updateBlock(blocked)
                }

                else -> {}
            }
        }
    }

    @Throws(Exception::class)
    fun Register(User: String, UserName: String, Password: String) {
        val outputStreamWriter = OutputStreamWriter(outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        bufferedWriter.write("Register:$User:$UserName:$Password\r\n")
        bufferedWriter.flush()
    }

    @Throws(Exception::class)
    fun Login(UserName: String, Password: String) {
        val outputStreamWriter = OutputStreamWriter(outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        bufferedWriter.write("Login:$UserName:$Password\r\n")
        bufferedWriter.flush()
    }

    @Throws(Exception::class)
    fun friendReq(username: String) {
        val outputStreamWriter = OutputStreamWriter(outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        bufferedWriter.write(
            """
                Friend Request:$userName:$username
                
                """.trimIndent()
        )
        bufferedWriter.flush()
    }

    @Throws(Exception::class)
    fun acceptReq(username: String) {
        val outputStreamWriter = OutputStreamWriter(outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        bufferedWriter.write(
            """
                Accept Request:$userName:$username
                
                """.trimIndent()
        )
        bufferedWriter.flush()
    }

    @Throws(Exception::class)
    fun oneToOneChat(username: String, message: String) {
        val outputStreamWriter = OutputStreamWriter(outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        bufferedWriter.write(
            """
                Private Message:$userName:$username:$message
                
                """.trimIndent()
        )
        bufferedWriter.flush()
    }

    @Throws(Exception::class)
    fun oneToSeveralChat(usersname: String, message: String) {
        val outputStreamWriter = OutputStreamWriter(outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        bufferedWriter.write(
            """
                Group Message:$userName:$usersname:$message
                
                """.trimIndent()
        )
        bufferedWriter.flush()
    }

    @Throws(Exception::class)
    fun JoinChat(username: String) {
        val outputStreamWriter = OutputStreamWriter(outputStream)
        val bufferedWriter = BufferedWriter(outputStreamWriter)
        bufferedWriter.write(
            """
                Join Chat:$userName:$username
                
                """.trimIndent()
        )
        bufferedWriter.flush()
    }

    fun updateChatName(chatName: String, newChat: String?) {
        var object1 = arrayOfNulls<Any>(3)
        for (x in chattingList.indices) {
            object1 = chattingList[x] as Array<Any?>
            if (object1[0] == chatName) {
                object1[0] = newChat
                chattingList[x] = object1
            }
        }
    }

    fun updateBlock(chatname: String) {
        var object1 = arrayOfNulls<Any>(3)
        for (i in chattingList.indices) {
            object1 = chattingList[i] as Array<Any?>
            if (object1[0] == chatname) {
                object1[2] = "Blocked"
                (object1[1] as JTextArea?)!!.append(
                    """
                        You are blocked from this chat
                        
                        """.trimIndent()
                )
                (object1[1] as JTextArea?)!!.append(
                    """
                        You can't send or recieve message
                        
                        """.trimIndent()
                )
                chattingList[i] = object1
            }
        }
    }

    fun chatPage(username: String?) {
        val jTextArea = JTextArea()
        jTextArea.setBounds(5, 5, 385, 285)
        jTextArea.isEditable = false
        val obj = arrayOfNulls<Any>(3)
        obj[0] = username
        obj[1] = jTextArea
        obj[2] = ""
        chattingList.add(obj)
    }

    fun loginPage() {
        val PageNameLabel = JLabel("Login Screen")
        PageNameLabel.setBounds(130, 10, 200, 40)
        PageNameLabel.font = Font("Arial", Font.PLAIN, 25)
        val UserNameLabel = JLabel("User Name")
        UserNameLabel.setBounds(5, 60, 70, 40)
        val UserNametf = JTextField()
        UserNametf.setBounds(5, 90, 380, 40)
        val PasswordLabel = JLabel("Password")
        PasswordLabel.setBounds(5, 140, 70, 40)
        val PasswordField = JPasswordField()
        PasswordField.setBounds(5, 170, 380, 40)
        val LoginBtn = JButton("Login")
        LoginBtn.setBounds(80, 260, 100, 40)
        LoginBtn.background = Color.green
        LoginBtn.addActionListener {
            if (UserNametf.text == "" || PasswordField.text == "") {
                JOptionPane.showMessageDialog(loginJFrame, "Please provide username and password.")
            } else {
                try {
                    Login(UserNametf.text, PasswordField.text)
                } catch (e1: Exception) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace()
                }
            }
        }
        val RegisterBtn = JButton("Register")
        RegisterBtn.setBounds(220, 260, 100, 40)
        RegisterBtn.background = Color.orange
        RegisterBtn.addActionListener { registerPage() }
        loginJFrame.add(LoginBtn)
        loginJFrame.add(RegisterBtn)
        loginJFrame.add(UserNameLabel)
        loginJFrame.add(PageNameLabel)
        loginJFrame.add(UserNametf)
        loginJFrame.add(PasswordLabel)
        loginJFrame.add(PasswordField)
        loginJFrame.setSize(400, 400)
        loginJFrame.layout = null
        loginJFrame.isVisible = true
    }

    fun userPage(UserName: String?) {
        userFrame.title = "Client Name: $UserName"
        val UserNameLabel = JLabel("Enter User Name ")
        UserNameLabel.setBounds(5, 320, 150, 40)
        val UserNametf = JTextField()
        UserNametf.setBounds(5, 350, 400, 40)
        jList.setBounds(0, 0, 600, 300)
        jList.background = Color.pink
        val JoinChatBtn = JButton("Join Group")
        JoinChatBtn.setBounds(480, 310, 100, 30)
        JoinChatBtn.background = Color.orange
        JoinChatBtn.addActionListener {
            val ChatName = JOptionPane.showInputDialog(userFrame, "Enter chat name")
            try {
                JoinChat(ChatName)
            } catch (e1: Exception) {
                // TODO Auto-generated catch block
                e1.printStackTrace()
            }
        }
        val AddBtn = JButton("Sent Request")
        AddBtn.setBounds(210, 420, 135, 30)
        AddBtn.background = Color.PINK
        AddBtn.addActionListener {
            if (UserNametf.text == "") {
                JOptionPane.showMessageDialog(userFrame, "Please provide user name")
            } else {
                try {
                    friendReq(UserNametf.text)
                } catch (e1: Exception) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace()
                }
                UserNametf.text = ""
            }
        }
        val mouseListener: MouseListener = object : MouseAdapter() {
            override fun mouseClicked(mouseEvent: MouseEvent) {
                val theList = mouseEvent.source as JList<*>
                if (mouseEvent.clickCount == 1) {
                    val index = theList.locationToIndex(mouseEvent.point)
                    if (index >= 0) {
                        val o = theList.model.getElementAt(index)
                        val username = o.toString()
                        for (x in chattingList.indices) {
                            var obj = arrayOfNulls<Any>(2)
                            obj = chattingList[x] as Array<Any?>
                            if (username == obj[0]) {
                                chatPage(username, obj[1] as JTextArea?)
                            }
                        }
                    }
                }
            }
        }
        jList.addMouseListener(mouseListener)
        userFrame.add(UserNameLabel)
        userFrame.add(UserNametf)
        userFrame.add(AddBtn)
        userFrame.add(JoinChatBtn)
        userFrame.add(jList)
        userFrame.setSize(600, 500)
        userFrame.layout = null
        userFrame.isVisible = true
    }

    fun registerPage() {
        val PageNameLabel = JLabel("Register Screen")
        PageNameLabel.setBounds(100, 10, 250, 40)
        PageNameLabel.font = Font("Arial", Font.PLAIN, 25)
        val NameLabel = JLabel("Name")
        NameLabel.setBounds(5, 40, 70, 40)
        val jTextField = JTextField()
        jTextField.setBounds(5, 70, 380, 40)
        val UserNameLabel = JLabel("User Name")
        UserNameLabel.setBounds(5, 120, 70, 40)
        val UserNametf = JTextField()
        UserNametf.setBounds(5, 150, 380, 40)
        val PasswordLabel = JLabel("Password")
        PasswordLabel.setBounds(5, 200, 70, 40)
        val PasswordField = JPasswordField()
        PasswordField.setBounds(5, 230, 380, 40)
        val RegisterBtn = JButton("Register")
        RegisterBtn.setBounds(135, 300, 130, 50)
        RegisterBtn.background = Color.orange
        RegisterBtn.addActionListener {
            if (jTextField.text == "" || UserNametf.text == "" || PasswordField.text == "") {
                JOptionPane.showMessageDialog(loginJFrame, "Please provide name ,username and password.")
            } else {
                try {
                    Register(jTextField.text, UserNametf.text, PasswordField.text)
                } catch (e1: Exception) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace()
                }
            }
        }
        registerFrame.add(NameLabel)
        registerFrame.add(PageNameLabel)
        registerFrame.add(jTextField)
        registerFrame.add(UserNameLabel)
        registerFrame.add(UserNametf)
        registerFrame.add(PasswordLabel)
        registerFrame.add(PasswordField)
        registerFrame.add(RegisterBtn)
        registerFrame.setSize(400, 400)
        registerFrame.layout = null
        registerFrame.isVisible = true
    }

    fun chatPage(name: String, jTextArea: JTextArea?) {
        val jFrame = JFrame()
        val contentPane = jFrame.contentPane
        contentPane.background = Color.LIGHT_GRAY
        jFrame.title = name
        val jTextField = JTextField()
        jTextField.setBounds(5, 300, 300, 50)
        val SendBtn = JButton("Send")
        SendBtn.setBounds(310, 310, 70, 30)
        SendBtn.background = Color.green
        SendBtn.addActionListener {
            val str = """
                You: ${jTextField.text}
                
                """.trimIndent()
            try {
                var check: String? = ""
                for (x in chattingList.indices) {
                    var obj = arrayOfNulls<Any>(3)
                    obj = chattingList[x] as Array<Any?>
                    if (obj[0] == name) {
                        check = obj[2] as String?
                    }
                }
                if (check != "Blocked") {
                    jTextArea!!.append(str)
                    val Parse = name.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    if (Parse.size > 1) {
                        oneToSeveralChat(name, jTextField.text)
                    } else {
                        oneToOneChat(name, jTextField.text)
                    }
                }
            } catch (e1: Exception) {
                // TODO Auto-generated catch block
                e1.printStackTrace()
            }
            jTextField.text = ""
        }
        jFrame.add(jTextField)
        jFrame.add(SendBtn)
        jFrame.add(jTextArea)
        jFrame.setSize(400, 400)
        jFrame.layout = null
        jFrame.isVisible = true
    }
}