package com.github.nduyhai.wintermind.toolWindow

import com.github.nduyhai.wintermind.services.WinterMindProjectService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import org.intellij.plugins.markdown.ui.preview.MarkdownPreviewFileEditor
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class WinterMindWindow(project: Project, toolWindow: ToolWindow) {
    private val aiService = toolWindow.project.service<WinterMindProjectService>()
    private val file = LightVirtualFile("preview.md")
    private val preview = MarkdownPreviewFileEditor(project, file)

    private val chatHistoryPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = JBUI.CurrentTheme.CustomFrameDecorations.paneBackground()
    }

    private val chatScrollPane = JBScrollPane(chatHistoryPanel).apply {
        verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    }

    private val inputArea = JBTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
        rows = 3
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    }

    private val sendButton = JButton("Send").apply {
        addActionListener { sendMessage() }
    }

    private val typingIndicator = JLabel("AI is thinking...").apply {
        isVisible = false
        foreground = JBColor.GRAY
        border = JBUI.Borders.empty(5, 10)
    }

    init {
        setupKeyboardShortcut()
    }

    fun getContent(): JPanel {
        return panel {
            row {
                cell(chatScrollPane)
                    .resizableColumn()
                    .align(AlignX.FILL)
                    .align(AlignY.FILL)
            }
            row {
                cell(typingIndicator)
                    .align(AlignX.LEFT)
            }
            row {
                cell(
                    JPanel(BorderLayout()).apply {
                        add(JBScrollPane(inputArea), BorderLayout.CENTER)
                        add(sendButton, BorderLayout.EAST)
                        preferredSize = Dimension(0, 100)
                        border = JBUI.Borders.empty(5)
                    }
                )
                    .resizableColumn()
                    .align(AlignX.FILL)
            }
        }
    }

    private fun addMessage(isUser: Boolean, content: String) {
        val messagePanel = createMessagePanel(isUser, content)
        chatHistoryPanel.add(messagePanel)
        chatHistoryPanel.revalidate()
        chatHistoryPanel.repaint()
        scrollToBottom()
    }

    private fun createMessagePanel(isUser: Boolean, content: String): JPanel {
        val messagePanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createCompoundBorder(
                JBUI.Borders.empty(5),
                JBUI.Borders.customLine(
                    JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground(),
                    1, 1, 1, 1
                )
            )
            background = if (isUser)
                JBUI.CurrentTheme.CustomFrameDecorations.paneBackground()
            else
                JBUI.CurrentTheme.CustomFrameDecorations.paneBackground().brighter()
        }

        val markdownContent = if (isUser) {
            """
            ### You:            ```
            $content            ```
            """
        } else {
            """
            ### Assistant:
            $content
            """
        }

        file.setContent(null, markdownContent, false)
        messagePanel.add(
            preview.component,
            if (isUser) BorderLayout.EAST else BorderLayout.WEST
        )

        return messagePanel
    }

    private fun sendMessage() {
        val text = inputArea.text.trim()
        if (text.isNotEmpty()) {
            // Disable input controls
            setInputControlsEnabled(false)
            showTypingIndicator(true)

            // Add user message
            addMessage(true, text)

            // Process in background
            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val response = aiService.prompt(text)

                    ApplicationManager.getApplication().invokeLater {
                        addMessage(false, response)
                        inputArea.text = ""
                        setInputControlsEnabled(true)
                        showTypingIndicator(false)
                        inputArea.requestFocus()
                    }
                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        addMessage(false, "Error: ${e.message}")
                        setInputControlsEnabled(true)
                        showTypingIndicator(false)
                    }
                }
            }
        }
    }

    private fun setInputControlsEnabled(enabled: Boolean) {
        inputArea.isEnabled = enabled
        sendButton.isEnabled = enabled
    }

    private fun showTypingIndicator(visible: Boolean) {
        typingIndicator.isVisible = visible
    }

    private fun scrollToBottom() {
        SwingUtilities.invokeLater {
            val vertical = chatScrollPane.verticalScrollBar
            vertical.value = vertical.maximum
        }
    }

    private fun setupKeyboardShortcut() {
        inputArea.inputMap.put(KeyStroke.getKeyStroke("control ENTER"), "send")
        inputArea.actionMap.put("send", object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                sendMessage()
            }
        })
    }

    fun dispose() {
        // Clean up resources if needed
    }
}