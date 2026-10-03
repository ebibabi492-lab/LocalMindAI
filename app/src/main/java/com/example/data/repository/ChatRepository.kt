package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepository(private val chatDao: ChatDao) {

    fun getConversations(): Flow<List<ConversationEntity>> = chatDao.getAllConversations()

    suspend fun getConversationById(id: Long): ConversationEntity? = withContext(Dispatchers.IO) {
        chatDao.getConversationById(id)
    }

    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> =
        chatDao.getMessagesForConversation(conversationId)

    suspend fun createConversation(title: String, modelId: String): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        chatDao.insertConversation(
            ConversationEntity(
                title = title,
                modelId = modelId,
                createdAt = now,
                lastUpdatedAt = now
            )
        )
    }

    suspend fun updateConversationTitle(id: Long, newTitle: String) = withContext(Dispatchers.IO) {
        val existing = chatDao.getConversationById(id) ?: return@withContext
        chatDao.updateConversation(
            existing.copy(
                title = newTitle,
                lastUpdatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addMessage(
        conversationId: Long,
        role: String,
        content: String
    ): Long = withContext(Dispatchers.IO) {
        val msgId = chatDao.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                role = role,
                content = content,
                timestamp = System.currentTimeMillis()
            )
        )
        // Update conversation last updated time
        val conv = chatDao.getConversationById(conversationId)
        if (conv != null) {
            chatDao.updateConversation(conv.copy(lastUpdatedAt = System.currentTimeMillis()))
        }
        msgId
    }

    suspend fun updateMessageContent(messageId: Long, conversationId: Long, role: String, content: String) = withContext(Dispatchers.IO) {
        chatDao.updateMessage(
            MessageEntity(
                id = messageId,
                conversationId = conversationId,
                role = role,
                content = content,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteConversation(id: Long) = withContext(Dispatchers.IO) {
        chatDao.deleteConversationById(id)
    }

    suspend fun clearAllConversations() = withContext(Dispatchers.IO) {
        chatDao.clearAllConversations()
    }
}
