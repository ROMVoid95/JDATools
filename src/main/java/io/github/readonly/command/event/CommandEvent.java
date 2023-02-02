/*
 * This file is part of JDATools, licensed under the MIT License (MIT).
 *
 * Copyright (c) ROMVoid95
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.github.readonly.command.event;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.github.readonly.api.ClientInterface;
import io.github.readonly.command.Client;
import io.github.readonly.command.ClientBuilder;
import io.github.readonly.command.Command;
import io.github.readonly.command.arg.parse.ArgumentIndex;
import io.github.readonly.common.util.ResultLevel;
import io.github.readonly.settings.GuildSettingsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.utils.Checks;

public class CommandEvent implements Event<MessageReceivedEvent>
{

	public static int MAX_MESSAGES = 6;

	private final MessageReceivedEvent	event;
	private final String				prefix;
	private ArgumentIndex				argumentIndex;
	private final ClientInterface		client;
	private Command						botCommand;

	/**
	 * Constructor for a CommandEvent.
	 * <p>
	 * <b>You should not call this!</b> <br>
	 * It is a generated wrapper for a {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent
	 * MessageReceivedEvent}.
	 *
	 * @param event
	 *            The initial MessageReceivedEvent
	 * @param prefix
	 *            The prefix used to execute this command
	 * @param argumentIndex
	 *            The String arguments after the command call
	 * @param client
	 *            The {@link io.github.readonly.api.ClientInterface Client}
	 */
	public CommandEvent(MessageReceivedEvent event, String prefix, ArgumentIndex argumentIndex, ClientInterface client)
	{
		this.event = event;
		this.prefix = prefix;
		this.argumentIndex = argumentIndex;
		this.client = client;
	}

	public CommandEvent(MessageReceivedEvent event, String prefix, ArgumentIndex argumentIndex, ClientInterface client, Command botCommand)
	{
		this(event, prefix, argumentIndex, client);
		this.botCommand = botCommand;
	}

	public Command getCommand()
	{
		return botCommand;
	}

	/**
	 * Returns the user's ArgumentIndex for the command. <br>
	 * If no arguments have been supplied, then this will return an empty index.
	 *
	 * @return Never-null ArgumentIndex that a user has supplied to a command
	 */
	public ArgumentIndex getArgumentIndex()
	{
		return argumentIndex;
	}

	public void setArgumentIndex(ArgumentIndex argumentIndex)
	{
		this.argumentIndex = argumentIndex;
	}

	/**
	 * Returns the prefix used when this event was raised. Useful for figuring out what people invoked.
	 *
	 * @return Never null prefix used to raise event
	 */
	public String getPrefix()
	{
		return prefix;
	}

	/**
	 * Returns the underlying {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent MessageReceivedEvent} for
	 * this CommandEvent.
	 *
	 * @return The underlying MessageReceivedEvent
	 */
	@Override
	public MessageReceivedEvent getEvent()
	{
		return event;
	}

	/**
	 * Returns the {@link io.github.readonly.api.ClientInterface Client} that initiated this CommandEvent.
	 *
	 * @return The initiating Client
	 */
	public ClientInterface getClient()
	{
		return client;
	}

	/**
	 * Links a {@link net.dv8tion.jda.api.entities.Message Message} with the calling Message contained by this
	 * CommandEvent.
	 * <p>
	 * This method is exposed for those who wish to use linked deletion but may require usage of
	 * {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendMessage(MessageCreateData)
	 * MessageChannel#sendMessage()} or for other reasons cannot use the standard {@code reply()} methods.
	 * <p>
	 * If the Message provided is <b>not</b> from the bot (IE: {@link net.dv8tion.jda.api.entities.SelfUser SelfUser}),
	 * an {@link java.lang.IllegalArgumentException IllegalArgumentException} will be thrown.
	 *
	 * @param message
	 *            The Message to add, must be from the SelfUser while linked deletion is being used.
	 *
	 * @throws java.lang.IllegalArgumentException
	 *             If the Message provided is not from the bot.
	 */
	public void linkId(Message message)
	{
		Checks.check(message.getAuthor().equals(getSelfUser()), "Attempted to link a Message who's author was not the bot!");
		((Client) client).linkIds(event.getMessageIdLong(), message);
	}

	// functional calls

	/**
	 * Replies with a String message.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * <b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in two split Messages.
	 *
	 * @param message
	 *            A String message to reply with
	 */
	public void reply(String message)
	{
		sendMessage(event.getChannel(), message);
	}

	public void replyNonEmbed(String message)
	{
		sendNonEmbed(event.getChannel(), message);
	}

	/**
	 * Replies with a String message and then queues a {@link java.util.function.Consumer}.
	 *
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the provided Consumer as it's
	 * success callback.
	 *
	 * <p>
	 * <b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in two split Messages. <br>
	 * The Consumer will be applied to the last message sent if this occurs.
	 *
	 * @param message
	 *            A String message to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 */
	public void reply(String message, Consumer<Message> success)
	{
		sendMessage(event.getChannel(), message, success);
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.Message Message} and then queues a
	 * {@link java.util.function.Consumer}.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#success()} with the provided Consumer as it's
	 * success callback.
	 *
	 * @param message
	 *            The Message to reply with
	 * @param success
	 *            The Consumer to success after sending the Message is sent.
	 */
	public void reply(MessageCreateData message, Consumer<Message> success)
	{
		if (this.getClient().makeAllRepliesEmbeded())
		{
			event.getChannel().sendMessageEmbeds(message.getEmbeds()).queue(m ->
			{
				if (event.isFromType(ChannelType.TEXT))
				{
					linkId(m);
				}
				success.accept(m);
			});
		} else
		{
			event.getChannel().sendMessage(message).queue(m ->
			{
				if (event.isFromType(ChannelType.TEXT))
				{
					linkId(m);
				}
				success.accept(m);
			});
		}
	}

	/**
	 * Replies with a String message and then queues a {@link java.util.function.Consumer}.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the first Consumer as it's
	 * success callback and the second Consumer as the failure callback.
	 * <p>
	 * <b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in two split Messages. <br>
	 * Either Consumer will be applied to the last message sent if this occurs.
	 *
	 * @param message
	 *            A String message to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 * @param failure
	 *            The Consumer to run if an error occurs when sending the Message.
	 */
	public void reply(String message, Consumer<Message> success, Consumer<Throwable> failure)
	{
		sendMessage(event.getChannel(), message, success, failure);
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 *
	 * @param embed
	 *            The MessageEmbed to reply with
	 */
	public void reply(MessageEmbed embed)
	{
		event.getChannel().sendMessageEmbeds(embed).queue(m ->
		{
			if (event.isFromType(ChannelType.TEXT))
			{
				linkId(m);
			}
		});
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} and then queues a
	 * {@link java.util.function.Consumer}.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the provided Consumer as it's
	 * success callback.
	 *
	 * @param embed
	 *            The MessageEmbed to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 */
	public void reply(MessageEmbed embed, Consumer<Message> success)
	{
		event.getChannel().sendMessageEmbeds(embed).queue(m ->
		{
			if (event.isFromType(ChannelType.TEXT))
			{
				linkId(m);
			}
			success.accept(m);
		});
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} and then queues a
	 * {@link java.util.function.Consumer}.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the first Consumer as it's
	 * success callback and the second Consumer as the failure callback.
	 *
	 * @param embed
	 *            The MessageEmbed to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 * @param failure
	 *            The Consumer to run if an error occurs when sending the Message.
	 */
	public void reply(MessageEmbed embed, Consumer<Message> success, Consumer<Throwable> failure)
	{
		event.getChannel().sendMessageEmbeds(embed).queue(m ->
		{
			if (event.isFromType(ChannelType.TEXT))
			{
				linkId(m);
			}
			success.accept(m);
		}, failure);
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.Message Message}.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 *
	 * @param message
	 *            The Message to reply with
	 */
	public void reply(MessageCreateData message)
	{
		if (this.getClient().makeAllRepliesEmbeded())
		{
			event.getChannel().sendMessageEmbeds(message.getEmbeds()).queue(m ->
			{
				if (event.isFromType(ChannelType.TEXT))
				{
					linkId(m);
				}
			});
		} else
		{
			event.getChannel().sendMessage(message).queue(m ->
			{
				if (event.isFromType(ChannelType.TEXT))
				{
					linkId(m);
				}
			});
		}
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.Message Message} and then queues a
	 * {@link java.util.function.Consumer}.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the first Consumer as it's
	 * success callback and the second Consumer as the failure callback.
	 *
	 * @param message
	 *            The Message to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 * @param failure
	 *            The Consumer to run if an error occurs when sending the Message.
	 */
	public void reply(MessageCreateData message, Consumer<Message> success, Consumer<Throwable> failure)
	{
		if (this.getClient().makeAllRepliesEmbeded())
		{
			event.getChannel().sendMessageEmbeds(message.getEmbeds()).queue(m ->
			{
				if (event.isFromType(ChannelType.TEXT))
				{
					linkId(m);
				}
				success.accept(m);
			}, failure);
		} else
		{
			event.getChannel().sendMessage(message).queue(m ->
			{
				if (event.isFromType(ChannelType.TEXT))
				{
					linkId(m);
				}
				success.accept(m);
			}, failure);
		}
	}

	/**
	 * Replies with a {@link java.io.File} with the provided name, or a default name if left null.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * This method uses
	 * {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendFiles(FileUpload... files)
	 * MessageChannel#sendFiles(FileUpload... files)} to send the File. For more information on what a bot may send
	 * using this, you may find the info in that method.
	 *
	 * @param file
	 *            The File to reply with
	 * @param filename
	 *            The filename that Discord should display (null for default).
	 */
	public void reply(File file, String filename)
	{
		event.getChannel().sendFiles(FileUpload.fromData(file, filename)).queue();
	}

	/**
	 * Replies with a String message and a {@link java.io.File} with the provided name, or a default name if left null.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * This method uses
	 * {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendFiles(FileUpload... files)
	 * MessageChannel#sendFiles(FileUpload... files)} to send the File. For more information on what a bot
	 * may send using this, you may find the info in that method.
	 *
	 * @param message
	 *            A String message to reply with
	 * @param file
	 *            The File to reply with
	 * @param filename
	 *            The filename that Discord should display (null for default).
	 */
	public void reply(String message, File file, String filename)
	{
		event.getChannel().sendFiles(FileUpload.fromData(file, filename)).addContent(message).queue();
	}

	/**
	 * Replies with a formatted String message using the provided arguments.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * <b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in two split Messages.
	 *
	 * @param format
	 *            A formatted String
	 * @param args
	 *            The arguments to use with the format
	 */
	public void replyFormatted(String format, Object... args)
	{
		sendMessage(event.getChannel(), String.format(format, args));
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} if possible, or just a String
	 * message if it cannot send the embed.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * <b>NOTE:</b> This alternate String message can exceed the 2000 character cap, and will be sent in two split
	 * Messages.
	 *
	 * @param embed
	 *            The MessageEmbed to reply with
	 * @param alternateMessage
	 *            A String message to reply with if the provided MessageEmbed cannot be sent
	 */
	public void replyOrAlternate(MessageEmbed embed, String alternateMessage)
	{
		try
		{
			event.getChannel().sendMessageEmbeds(embed).queue();
		} catch (PermissionException e)
		{
			reply(alternateMessage);
		}
	}

	/**
	 * Replies with a String message and a {@link java.io.File} with the provided name, or a default name if left null.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * This method uses
	 * {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendFiles(FileUpload... files)
	 * MessageChannel#sendFiles(FileUpload... files)} to send the File. For more information on what a bot
	 * may send using this, you may find the info in that method.
	 * <p>
	 * <b>NOTE:</b> This alternate String message can exceed the 2000 character cap, and will be sent in two split
	 * Messages.
	 * <p>
	 * It is also worth noting that unlike {@link io.github.readonly.command.event.CommandEvent#reply(File,String)
	 * CommandEvent#reply(File, String)} and {@link io.github.readonly.command.event.CommandEvent#reply(String,File,String)
	 * CommandEvent#reply(String, File, String)}, this method does not throw a {@link java.io.IOException}. This is
	 * because the cause of the alternate String message being sent comes directly from a thrown
	 * {@link java.lang.Exception}, and thus a thrown IOException is grounds for the sending of the alternate message.
	 *
	 * @param message
	 *            A String message to reply with
	 * @param file
	 *            The File to reply with
	 * @param filename
	 *            The filename that Discord should display (null for default).
	 * @param alternateMessage
	 *            A String message to reply with if the file cannot be uploaded, or an {@link java.io.IOException} is
	 *            thrown
	 */
	public void replyOrAlternate(String message, File file, String filename, String alternateMessage)
	{
		try
		{
			event.getChannel().sendFiles(FileUpload.fromData(file, filename)).addContent(message).queue();
		} catch (Exception e)
		{
			reply(alternateMessage);
		}
	}

	/**
	 * Replies with a String message sent to the calling {@link net.dv8tion.jda.api.entities.User User}'s
	 * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
	 * <p>
	 * If the User to be Direct Messaged does not already have a PrivateChannel open to send messages to, this method
	 * will automatically open one.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * <b>NOTE:</b> This alternate String message can exceed the 2000 character cap, and will be sent in two split
	 * Messages.
	 *
	 * @param message
	 *            A String message to reply with
	 */
	public void replyInDm(String message)
	{
		if (event.isFromType(ChannelType.PRIVATE))
		{
			reply(message);
		} else
		{
			event.getAuthor().openPrivateChannel().queue(pc -> sendMessage(pc, message));
		}
	}

	/**
	 * Replies with a String message sent to the calling {@link net.dv8tion.jda.api.entities.User User}'s
	 * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
	 * <p>
	 * If the User to be Direct Messaged does not already have a PrivateChannel open to send messages to, this method
	 * will automatically open one.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the provided Consumer as it's
	 * success callback.
	 * <p>
	 * <b>NOTE:</b> This alternate String message can exceed the 2000 character cap, and will be sent in two split
	 * Messages.
	 *
	 * @param message
	 *            A String message to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 */
	public void replyInDm(String message, Consumer<Message> success)
	{
		if (event.isFromType(ChannelType.PRIVATE))
		{
			reply(message, success);
		} else
		{
			event.getAuthor().openPrivateChannel().queue(pc -> sendMessage(pc, message, success));
		}
	}

	/**
	 * Replies with a String message sent to the calling {@link net.dv8tion.jda.api.entities.User User}'s
	 * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
	 * <p>
	 * If the User to be Direct Messaged does not already have a PrivateChannel open to send messages to, this method
	 * will automatically open one.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the first Consumer as it's
	 * success callback and the second Consumer as the failure callback.
	 * <p>
	 * <b>NOTE:</b> This alternate String message can exceed the 2000 character cap, and will be sent in two split
	 * Messages.
	 *
	 * @param message
	 *            A String message to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 * @param failure
	 *            The Consumer to run if an error occurs when sending the Message.
	 */
	public void replyInDm(String message, Consumer<Message> success, Consumer<Throwable> failure)
	{
		if (event.isFromType(ChannelType.PRIVATE))
		{
			reply(message, success, failure);
		} else
		{
			event.getAuthor().openPrivateChannel().queue(pc -> sendMessage(pc, message, success, failure), failure);
		}
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} sent to the calling
	 * {@link net.dv8tion.jda.api.entities.User User}'s
	 * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
	 * <p>
	 * If the User to be Direct Messaged does not already have a PrivateChannel open to send messages to, this method
	 * will automatically open one.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 *
	 * @param embed
	 *            The MessageEmbed to reply with
	 */
	public void replyInDm(MessageEmbed embed)
	{
		if (event.isFromType(ChannelType.PRIVATE))
		{
			reply(embed);
		} else
		{
			event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue());
		}
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} sent to the calling
	 * {@link net.dv8tion.jda.api.entities.User User}'s
	 * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
	 * <p>
	 * If the User to be Direct Messaged does not already have a PrivateChannel open to send messages to, this method
	 * will automatically open one.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the provided Consumer as it's
	 * success callback.
	 *
	 * @param embed
	 *            The MessageEmbed to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 */
	public void replyInDm(MessageEmbed embed, Consumer<Message> success)
	{
		if (event.isFromType(ChannelType.PRIVATE))
		{
			getPrivateChannel().sendMessageEmbeds(embed).queue(success);
		} else
		{
			event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue(success));
		}
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} sent to the calling
	 * {@link net.dv8tion.jda.api.entities.User User}'s
	 * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
	 * <p>
	 * If the User to be Direct Messaged does not already have a PrivateChannel open to send messages to, this method
	 * will automatically open one.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the first Consumer as it's
	 * success callback and the second Consumer as the failure callback.
	 *
	 * @param embed
	 *            The MessageEmbed to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 * @param failure
	 *            The Consumer to run if an error occurs when sending the Message.
	 */
	public void replyInDm(MessageEmbed embed, Consumer<Message> success, Consumer<Throwable> failure)
	{
		if (event.isFromType(ChannelType.PRIVATE))
		{
			getPrivateChannel().sendMessageEmbeds(embed).queue(success, failure);
		} else
		{
			event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue(success, failure), failure);
		}
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.Message Message} sent to the calling
	 * {@link net.dv8tion.jda.api.entities.User User}'s
	 * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
	 * <p>
	 * If the User to be Direct Messaged does not already have a PrivateChannel open to send messages to, this method
	 * will automatically open one.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 *
	 * @param message
	 *            The MessageCreateData to reply with
	 */
	public void replyInDm(MessageCreateData message)
	{
		if (event.isFromType(ChannelType.PRIVATE))
		{
			reply(message);
		} else
		{
			event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(message).queue());
		}
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.Message Message} sent to the calling
	 * {@link net.dv8tion.jda.api.entities.User User}'s
	 * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
	 * <p>
	 * If the User to be Direct Messaged does not already have a PrivateChannel open to send messages to, this method
	 * will automatically open one.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the provided Consumer as it's
	 * success callback.
	 *
	 * @param message
	 *            The MessageCreateData to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 */
	public void replyInDm(MessageCreateData message, Consumer<Message> success)
	{
		if (event.isFromType(ChannelType.PRIVATE))
		{
			getPrivateChannel().sendMessage(message).queue(success);
		} else
		{
			event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(message).queue(success));
		}
	}

	/**
	 * Replies with a {@link net.dv8tion.jda.api.entities.Message Message} sent to the calling
	 * {@link net.dv8tion.jda.api.entities.User User}'s
	 * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
	 * <p>
	 * If the User to be Direct Messaged does not already have a PrivateChannel open to send messages to, this method
	 * will automatically open one.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the first Consumer as it's
	 * success callback and the second Consumer as the failure callback.
	 *
	 * @param message
	 *            The MessageCreateData to reply with
	 * @param success
	 *            The Consumer to queue after sending the Message is sent.
	 * @param failure
	 *            The Consumer to run if an error occurs when sending the Message.
	 */
	public void replyInDm(MessageCreateData message, Consumer<Message> success, Consumer<Throwable> failure)
	{
		if (event.isFromType(ChannelType.PRIVATE))
		{
			getPrivateChannel().sendMessage(message).queue(success, failure);
		} else
		{
			event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(message).queue(success, failure), failure);
		}
	}

	/**
	 * Replies with a String message and a {@link java.io.File} with the provided name, or a default name if left null,
	 * and sent to the calling {@link net.dv8tion.jda.api.entities.User User}'s
	 * {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel}.
	 * <p>
	 * If the User to be Direct Messaged does not already have a PrivateChannel open to send messages to, this method
	 * will automatically open one.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * This method uses
	 * {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel#sendFiles(FileUpload... files)
	 * MessageChannel#sendFiles(FileUpload... files)} to send the File. For more information on what a bot
	 * may send using this, you may find the info in that method.
	 *
	 * @param message
	 *            A String message to reply with
	 * @param file
	 *            The {@code File} to reply with
	 * @param filename
	 *            The filename that Discord should display (null for default).
	 */
	public void replyInDm(String message, File file, String filename)
	{
		if (event.isFromType(ChannelType.PRIVATE))
		{
			reply(message, file, filename);
		} else
		{
			event.getAuthor().openPrivateChannel().queue(pc -> pc.sendFiles(FileUpload.fromData(file, filename)).addContent(message).queue());
		}
	}

	/**
	 * Replies with a String message, and a prefixed success emoji.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * <b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in two split Messages.
	 *
	 * @param message
	 *            A String message to reply with
	 */
	public void replySuccess(String message)
	{
		reply(client.getSuccess() + " " + message);
	}

	public void replySuccess(EmbedBuilder messageEmbed)
	{
		reply(messageEmbed.setColor(ResultLevel.SUCCESS.getColor()).build());
	}

	/**
	 * Replies with a String message and a prefixed success emoji and then queues a {@link java.util.function.Consumer}.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the provided Consumer as it's
	 * success callback.
	 * <p>
	 * <b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in two split Messages.
	 *
	 * @param message
	 *            A String message to reply with
	 * @param queue
	 *            The Consumer to queue after sending the Message is sent.
	 */
	public void replySuccess(String message, Consumer<Message> queue)
	{
		reply(client.getSuccess() + " " + message, queue);
	}

	/**
	 * Replies with a String message, and a prefixed warning emoji.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * <b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in two split Messages.
	 *
	 * @param message
	 *            A String message to reply with
	 */
	public void replyWarning(String message)
	{
		reply(client.getWarning() + " " + message);
	}

	public void replyWarning(EmbedBuilder messageEmbed)
	{
		reply(messageEmbed.setColor(ResultLevel.WARNING.getColor()).build());
	}

	/**
	 * Replies with a String message and a prefixed warning emoji and then queues a {@link java.util.function.Consumer}.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the provided Consumer as it's
	 * success callback.
	 * <p>
	 * <b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in two split Messages.
	 *
	 * @param message
	 *            A String message to reply with
	 * @param queue
	 *            The Consumer to queue after sending the Message is sent.
	 */
	public void replyWarning(String message, Consumer<Message> queue)
	{
		reply(client.getWarning() + " " + message, queue);
	}

	/**
	 * Replies with a String message and a prefixed error emoji.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()}.
	 * <p>
	 * <b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in two split Messages.
	 *
	 * @param message
	 *            A String message to reply with
	 */
	public void replyError(String message)
	{
		reply(client.getError() + " " + message);
	}

	public void replyError(EmbedBuilder messageEmbed)
	{
		reply(messageEmbed.setColor(ResultLevel.ERROR.getColor()).build());
	}

	/**
	 * Replies with a String message and a prefixed error emoji and then queues a {@link java.util.function.Consumer}.
	 * <p>
	 * The {@link net.dv8tion.jda.api.requests.RestAction RestAction} returned by sending the response as a
	 * {@link net.dv8tion.jda.api.entities.Message Message} automatically does
	 * {@link net.dv8tion.jda.api.requests.RestAction#queue() RestAction#queue()} with the provided Consumer as it's
	 * success callback.
	 * <p>
	 * <b>NOTE:</b> This message can exceed the 2000 character cap, and will be sent in two split Messages.
	 *
	 * @param message
	 *            A String message to reply with
	 * @param queue
	 *            The Consumer to queue after sending the Message is sent.
	 */
	public void replyError(String message, Consumer<Message> queue)
	{
		reply(client.getError() + " " + message, queue);
	}

	/**
	 * Adds a success reaction to the calling {@link net.dv8tion.jda.api.entities.Message Message}.
	 */
	public void reactSuccess()
	{
		react(client.getSuccess());
	}

	/**
	 * Adds a warning reaction to the calling {@link net.dv8tion.jda.api.entities.Message Message}.
	 */
	public void reactWarning()
	{
		react(client.getWarning());
	}

	/**
	 * Adds an error reaction to the calling {@link net.dv8tion.jda.api.entities.Message Message}.
	 */
	public void reactError()
	{
		react(client.getError());
	}

	/**
	 * Uses the {@link io.github.readonly.api.ClientInterface#getScheduleExecutor() client's executor} to run the provided
	 * {@link java.lang.Runnable Runnable} asynchronously without blocking the thread this is called in.
	 * <p>
	 * The ScheduledExecutorService this runs on can be configured using
	 * {@link ClientBuilder#setScheduleExecutor(java.util.concurrent.ScheduledExecutorService)
	 * ClientBuilder#setScheduleExecutor(ScheduledExecutorService)}.
	 *
	 * @param runnable
	 *            The runnable to run async
	 */
	public void async(Runnable runnable)
	{
		Checks.notNull(runnable, "Runnable");
		client.getScheduleExecutor().submit(runnable);
	}

	// private methods

	private void react(String reaction)
	{
		if ((reaction == null) || reaction.isEmpty())
		{
			return;
		}
		try
		{
			event.getMessage().addReaction(Emoji.fromFormatted(reaction.replaceAll("<a?:(.+):(\\d+)>", "$1:$2"))).queue();
		} catch (PermissionException ignored)
		{
		}
	}

	private void sendNonEmbed(MessageChannel chan, String message)
	{
		ArrayList<String> messages = splitMessage(message);
		for (int i = 0; (i < MAX_MESSAGES) && (i < messages.size()); i++)
		{
			chan.sendMessage(messages.get(i)).queue(m ->
			{
				if (event.isFromType(ChannelType.TEXT))
				{
					linkId(m);
				}
			});
		}
	}

	private void sendMessage(MessageChannel chan, String message)
	{
		ArrayList<String> messages = splitMessage(message);
		for (int i = 0; (i < MAX_MESSAGES) && (i < messages.size()); i++)
		{
			if (this.getClient().makeAllRepliesEmbeded())
			{
				chan.sendMessageEmbeds(makeEmbed(messages.get(i))).queue(m ->
				{
					if (event.isFromType(ChannelType.TEXT))
					{
						linkId(m);
					}
				});
			} else
			{
				chan.sendMessage(messages.get(i)).queue(m ->
				{
					if (event.isFromType(ChannelType.TEXT))
					{
						linkId(m);
					}
				});
			}
		}
	}

	private void sendMessage(MessageChannel chan, String message, Consumer<Message> success)
	{
		ArrayList<String> messages = splitMessage(message);
		for (int i = 0; (i < MAX_MESSAGES) && (i < messages.size()); i++)
		{
			if (((i + 1) == MAX_MESSAGES) || ((i + 1) == messages.size()))
			{
				if (this.getClient().makeAllRepliesEmbeded())
				{
					chan.sendMessageEmbeds(makeEmbed(messages.get(i))).queue(m ->
					{
						if (event.isFromType(ChannelType.TEXT))
						{
							linkId(m);
						}
						success.accept(m);
					});
				} else
				{
					chan.sendMessage(messages.get(i)).queue(m ->
					{
						if (event.isFromType(ChannelType.TEXT))
						{
							linkId(m);
						}
						success.accept(m);
					});
				}
			} else
			{
				if (this.getClient().makeAllRepliesEmbeded())
				{
					chan.sendMessageEmbeds(makeEmbed(messages.get(i))).queue(m ->
					{
						if (event.isFromType(ChannelType.TEXT))
						{
							linkId(m);
						}
					});
				} else
				{
					chan.sendMessage(messages.get(i)).queue(m ->
					{
						if (event.isFromType(ChannelType.TEXT))
						{
							linkId(m);
						}
					});
				}
			}
		}
	}

	private void sendMessage(MessageChannel chan, String message, Consumer<Message> success, Consumer<Throwable> failure)
	{
		ArrayList<String> messages = splitMessage(message);
		for (int i = 0; (i < MAX_MESSAGES) && (i < messages.size()); i++)
		{
			if (((i + 1) == MAX_MESSAGES) || ((i + 1) == messages.size()))
			{
				if (this.getClient().makeAllRepliesEmbeded())
				{
					chan.sendMessageEmbeds(makeEmbed(messages.get(i))).queue(m ->
					{
						if (event.isFromType(ChannelType.TEXT))
						{
							linkId(m);
						}
						success.accept(m);
					}, failure);
				} else
				{
					chan.sendMessage(messages.get(i)).queue(m ->
					{
						if (event.isFromType(ChannelType.TEXT))
						{
							linkId(m);
						}
						success.accept(m);
					}, failure);
				}
			} else
			{
				if (this.getClient().makeAllRepliesEmbeded())
				{
					chan.sendMessageEmbeds(makeEmbed(messages.get(i))).queue(m ->
					{
						if (event.isFromType(ChannelType.TEXT))
						{
							linkId(m);
						}
					});
				} else
				{
					chan.sendMessage(messages.get(i)).queue(m ->
					{
						if (event.isFromType(ChannelType.TEXT))
						{
							linkId(m);
						}
					});
				}
			}
		}
	}

	private MessageEmbed makeEmbed(String message)
	{
		return new EmbedBuilder().setDescription(message).build();
	}

	/**
	 * Splits a String into one or more Strings who's length does not exceed 2000 characters. <br>
	 * Also nullifies usages of {@code @here} and {@code @everyone} so that they do not mention anyone. <br>
	 * Useful for splitting long messages so that they can be sent in more than one
	 * {@link net.dv8tion.jda.api.entities.Message Message} at maximum potential length.
	 *
	 * @param stringtoSend
	 *            The String to split and send
	 *
	 * @return An {@link java.util.ArrayList ArrayList} containing one or more Strings, with nullified occurrences of
	 *         {@code @here} and {@code @everyone}, and that do not exceed 2000 characters in length
	 */
	public static ArrayList<String> splitMessage(String stringtoSend)
	{
		ArrayList<String> msgs = new ArrayList<>();
		if (stringtoSend != null)
		{
			stringtoSend = stringtoSend.replace("@everyone", "@\u0435veryone").replace("@here", "@h\u0435re").trim();
			while (stringtoSend.length() > 2000)
			{
				int	leeway	= 2000 - (stringtoSend.length() % 2000);
				int	index	= stringtoSend.lastIndexOf("\n", 2000);
				if (index < leeway)
				{
					index = stringtoSend.lastIndexOf(" ", 2000);
				}
				if (index < leeway)
				{
					index = 2000;
				}
				String temp = stringtoSend.substring(0, index).trim();
				if (!temp.equals(""))
				{
					msgs.add(temp);
				}
				stringtoSend = stringtoSend.substring(index).trim();
			}
			if (!stringtoSend.equals(""))
			{
				msgs.add(stringtoSend);
			}
		}
		return msgs;
	}

	// custom shortcuts

	/**
	 * Gets a {@link net.dv8tion.jda.api.entities.SelfUser SelfUser} representing the bot. <br>
	 * This is the same as invoking {@code event.getJDA().getSelfUser()}.
	 *
	 * @return A User representing the bot
	 */
	public SelfUser getSelfUser()
	{
		return event.getJDA().getSelfUser();
	}

	/**
	 * Gets a {@link net.dv8tion.jda.api.entities.Member Member} representing the bot, or null if the event does not
	 * take place on a {@link net.dv8tion.jda.api.entities.Guild Guild}. <br>
	 * This is the same as invoking {@code event.getGuild().getSelfMember()}.
	 *
	 * @return A possibly-null Member representing the bot
	 */
	public Member getSelfMember()
	{
		return event.getGuild() == null ? null : event.getGuild().getSelfMember();
	}

	/**
	 * Tests whether or not the {@link net.dv8tion.jda.api.entities.User User} who triggered this event is an owner of
	 * the bot.
	 *
	 * @return {@code true} if the User is the Owner, else {@code false}
	 */
	public boolean isOwner()
	{
		if (event.getAuthor().getId().equals(this.getClient().getOwnerId()))
		{
			return true;
		}
		if (this.getClient().getCoOwnerIds() == null)
		{
			return false;
		}
		for (String id : this.getClient().getCoOwnerIds())
		{
			if (id.equals(event.getAuthor().getId()))
			{
				return true;
			}
		}
		return false;
	}

	// shortcuts

	/**
	 * Gets the settings of the guild in which this command was run.
	 *
	 * @param <S> the type of the settings
	 * @return the settings, or {@code null} if either of the following conditions are met:
	 * <ul>
	 *     <li>this command wasn't run in a guild</li>
	 *     <li>the client's {@link GuildSettingsManager} is null</li>
	 *     <li>the {@link GuildSettingsManager} returned null settings for the guild</li>
	 * </ul>
	 */
	@Nullable
	public <S> S getGuildSettings()
	{
		try {
			final GuildSettingsManager<S> manager = getClient().getSettingsManager();
			if (manager == null)
			{
				return null;
			}
			return manager.getSettings(getGuild());
		} catch (IllegalStateException ignored) {
			return null;
		}
	}

	/**
	 * Gets the settings of the guild in which this command was run.
	 *
	 * @param settingsClazz the class of the settings
	 * @param <S> the type of the settings
	 * @return the settings, or {@code null} if either of the following conditions are met:
	 * <ul>
	 *     <li>this command wasn't run in a guild</li>
	 *      <li>the client's {@link GuildSettingsManager} is null</li>
	 *      <li>the {@link GuildSettingsManager} returned null settings for the guild</li>
	 *      <li>the {@link GuildSettingsManager} returned settings that are not assignable to the {@code settingsClazz}</li>
	 * </ul>
	 */
	@Nullable
	@SuppressWarnings("rawtypes")
	public <S> S getGuildSettings(Class<? extends S> settingsClazz)
	{
		try {
			getGuild();
			final GuildSettingsManager manager = getClient().getSettingsManager();
			if (manager == null)
			{
				return null;
			}
			final Object settings = manager.getSettings(getGuild());
			if (!settingsClazz.isInstance(settings))
			{
				return null;
			}
			return settingsClazz.cast(settings);
		} catch (IllegalStateException ignored) {
			return null;
		}
	}

	/**
	 * Gets the {@link net.dv8tion.jda.api.entities.User User} who triggered this CommandEvent.
	 *
	 * @return The User who triggered this CommandEvent
	 */
	@Override
	public User getAuthor()
	{
		return event.getAuthor();
	}

	/**
	 * Gets the {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel MessageChannel} that the
	 * CommandEvent was triggered on.
	 *
	 * @return The MessageChannel that the CommandEvent was triggered on
	 */
	@Override
	public MessageChannel getChannel()
	{
		return event.getChannel();
	}

	/**
	 * Gets the {@link net.dv8tion.jda.api.entities.channel.ChannelType ChannelType} of the
	 * {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel MessageChannel} that the CommandEvent was
	 * triggered on.
	 *
	 * @return The ChannelType of the MessageChannel that this CommandEvent was triggered on
	 */
	public ChannelType getChannelType()
	{
		return event.getChannelType();
	}

	/**
	 * Gets the {@link net.dv8tion.jda.api.entities.Guild Guild} that this CommandEvent was triggered on.
	 *
	 * @return The Guild that this CommandEvent was triggered on
	 */
	public Guild getGuild()
	{
		return event.getGuild();
	}

	/**
	 * Gets the instance of {@link net.dv8tion.jda.api.JDA JDA} that this CommandEvent was caught by.
	 *
	 * @return The instance of JDA that this CommandEvent was caught by
	 */
	public JDA getJDA()
	{
		return event.getJDA();
	}

	/**
	 * Gets the {@link net.dv8tion.jda.api.entities.Member Member} that triggered this CommandEvent.
	 *
	 * @return The Member that triggered this CommandEvent
	 */
	public Member getMember()
	{
		return event.getMember();
	}

	/**
	 * Gets the {@link net.dv8tion.jda.api.entities.Message Message} responsible for triggering this CommandEvent.
	 *
	 * @return The Message responsible for the CommandEvent
	 */
	public Message getMessage()
	{
		return event.getMessage();
	}

	/**
	 * Gets the {@link net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel PrivateChannel} that this
	 * CommandEvent may have taken place on, or {@code null} if it didn't happen on a PrivateChannel.
	 *
	 * @return The PrivateChannel that this CommandEvent may have taken place on, or null if it did not happen on a
	 *         PrivateChannel.
	 */
	public PrivateChannel getPrivateChannel()
	{
		return event.getChannel().asPrivateChannel();
	}

	/**
	 * Gets the response number for the {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent
	 * MessageReceivedEvent}.
	 *
	 * @return The response number for the MessageReceivedEvent
	 */
	public long getResponseNumber()
	{
		return event.getResponseNumber();
	}

	/**
	 * Gets the {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel TextChannel} that this CommandEvent may
	 * have taken place on, or {@code null} if it didn't happen on a TextChannel.
	 *
	 * @return The TextChannel this CommandEvent may have taken place on, or null if it did not happen on a TextChannel.
	 */
	public TextChannel getTextChannel()
	{
		return event.getChannel().asTextChannel();
	}

	/**
	 * Compares a provided {@link net.dv8tion.jda.api.entities.channel.ChannelType ChannelType} with the one this
	 * CommandEvent occurred on, returning {@code true} if they are the same ChannelType.
	 *
	 * @param channelType
	 *            The ChannelType to compare
	 *
	 * @return {@code true} if the CommandEvent originated from a
	 *         {@link net.dv8tion.jda.api.entities.channel.middleman.MessageChannel} of the provided ChannelType,
	 *         otherwise {@code false}.
	 */
	public boolean isFromType(ChannelType channelType)
	{
		return event.isFromType(channelType);
	}
}
