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

import javax.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import io.github.readonly.command.Client;
import io.github.readonly.command.DiscordInfo;
import io.github.readonly.settings.GuildSettingsManager;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class SlashCommandEvent extends SlashCommandInteractionEvent implements Event<SlashCommandInteractionEvent>
{
	private final Client client;
	private final DiscordInfo info;

	public SlashCommandEvent(SlashCommandInteractionEvent event, Client client)
	{
		super(event.getJDA(), event.getResponseNumber(), event);
		this.info = DiscordInfo.of(event.getUser());
		this.client = client;
	}

	public DiscordInfo getInfo()
	{
		return info;
	}

	/**
	 * The {@link Client} that this event was triggered from.
	 *
	 * @return The Client that this event was triggered from
	 */
	public Client getClient()
	{
		return client;
	}

	/**
	 * Gets the provided Option Key as a String value, or returns {@code null}
	 * if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or null if the option is not present
	 */
	@Nullable
	public String optString(@NotNull String key)
	{
		return optString(key, null);
	}

	/**
	 * Gets the provided Option Key as a String value, or returns the default
	 * one if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            Nullable default value used in the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	@Nullable
	@Contract("_, !null -> !null")
	public String optString(@NotNull String key, @Nullable String defaultValue)
	{
		return getOption(key, defaultValue, OptionMapping::getAsString);
	}

	/**
	 * Gets the provided Option Key as a boolean value, or returns {@code false}
	 * if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or false if the option is not present
	 */
	public boolean optBoolean(@NotNull String key)
	{
		return optBoolean(key, false);
	}

	/**
	 * Gets the provided Option Key as a boolean value, or returns the default
	 * one if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            The fallback option in case of the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	public boolean optBoolean(@NotNull String key, boolean defaultValue)
	{
		return getOption(key, defaultValue, OptionMapping::getAsBoolean);
	}

	/**
	 * Gets the provided Option Key as a long value, or returns {@code 0} if the
	 * option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or 0 if the option is not present
	 */
	public long optLong(@NotNull String key)
	{
		return optLong(key, 0);
	}

	/**
	 * Gets the provided Option Key as a long value, or returns the default one
	 * if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            The fallback option in case of the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	public long optLong(@NotNull String key, long defaultValue)
	{
		return getOption(key, defaultValue, OptionMapping::getAsLong);
	}

	/**
	 * Gets the provided Option Key as a double value, or returns {@code 0.0} if
	 * the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or 0.0 if the option is not present
	 */
	public double optDouble(@NotNull String key)
	{
		return optDouble(key, 0.0);
	}

	/**
	 * Gets the provided Option Key as a double value, or returns the default
	 * one if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            The fallback option in case of the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	public double optDouble(@NotNull String key, double defaultValue)
	{
		return getOption(key, defaultValue, OptionMapping::getAsDouble);
	}

	/**
	 * Gets the provided Option Key as a GuildChannel value, or returns
	 * {@code null} if the option cannot be found.
	 * <br>
	 * This will <b>always</b> return null when the SlashCommandEvent was not
	 * executed in a Guild.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or null if the option is not present
	 */
	@Nullable
	public GuildChannel optGuildChannel(@NotNull String key)
	{
		return optGuildChannel(key, null);
	}

	/**
	 * Gets the provided Option Key as a GuildChannel value, or returns the
	 * default one if the option cannot be found.
	 * <br>
	 * This will <b>always</b> return the default value when the
	 * SlashCommandEvent was not executed in a Guild.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            Nullable default value used in the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	@Nullable
	@Contract("_, !null -> !null")
	public GuildChannel optGuildChannel(@NotNull String key, @Nullable GuildChannel defaultValue)
	{
		if (!isFromGuild())
		{
			return defaultValue;
		}

		return getOption(key, defaultValue, optionMapping -> optionMapping.getAsChannel().asStandardGuildChannel());
	}

	/**
	 * Gets the provided Option Key as a Member value, or returns {@code null}
	 * if the option cannot be found.
	 * <br>
	 * This will <b>always</b> return null when the SlashCommandEvent was not
	 * executed in a Guild.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or null if the option is not present
	 */
	@Nullable
	public Member optMember(@NotNull String key)
	{
		return optMember(key, null);
	}

	/**
	 * Gets the provided Option Key as a Member value, or returns the default
	 * one if the option cannot be found.
	 * <br>
	 * This will <b>always</b> return the default value when the
	 * SlashCommandEvent was not executed in a Guild.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            Nullable default value used in the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	@Nullable
	@Contract("_, !null -> !null")
	public Member optMember(@NotNull String key, @Nullable Member defaultValue)
	{
		if (!isFromGuild())
		{
			return defaultValue; // Non-guild commands do not have a member.
		}

		return getOption(key, defaultValue, OptionMapping::getAsMember);
	}

	/**
	 * Gets the provided Option Key as a IMentionable value, or returns
	 * {@code null} if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or null if the option is not present
	 */
	@Nullable
	public IMentionable optMentionable(@NotNull String key)
	{
		return optMentionable(key, null);
	}

	/**
	 * Gets the provided Option Key as a IMentionable value, or returns the
	 * default one if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            Nullable default value used in the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	@Nullable
	@Contract("_, !null -> !null")
	public IMentionable optMentionable(@NotNull String key, @Nullable IMentionable defaultValue)
	{
		return getOption(key, defaultValue, OptionMapping::getAsMentionable);
	}

	/**
	 * Gets the provided Option Key as a Role value, or returns {@code null} if
	 * the option cannot be found.
	 * <br>
	 * This will <b>always</b> return null when the SlashCommandEvent was not
	 * executed in a Guild.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or null if the option is not present
	 */
	@Nullable
	public Role optRole(@NotNull String key)
	{
		return optRole(key, null);
	}

	/**
	 * Gets the provided Option Key as a Role value, or returns the default one
	 * if the option cannot be found.
	 * <br>
	 * This will <b>always</b> return the default value when the
	 * SlashCommandEvent was not executed in a Guild.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            Nullable default value used in the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	@Nullable
	@Contract("_, !null -> !null")
	public Role optRole(@NotNull String key, @Nullable Role defaultValue)
	{
		if (!isFromGuild())
		{
			return defaultValue;
		}

		return getOption(key, defaultValue, OptionMapping::getAsRole);
	}

	/**
	 * Gets the provided Option Key as a User value, or returns {@code null} if
	 * the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or null if the option is not present
	 */
	@Nullable
	public User optUser(@NotNull String key)
	{
		return optUser(key, null);
	}

	/**
	 * Gets the provided Option Key as a User value, or returns the default one
	 * if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            Nullable default value used in the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	@Nullable
	@Contract("_, !null -> !null")
	public User optUser(@NotNull String key, @Nullable User defaultValue)
	{
		return getOption(key, defaultValue, OptionMapping::getAsUser);
	}

	/**
	 * Gets the provided Option Key as a MessageChannel value, or returns
	 * {@code null} if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or null if the option is not present
	 */
	@Nullable
	public MessageChannel optMessageChannel(@NotNull String key)
	{
		return optMessageChannel(key, null);
	}

	/**
	 * Gets the provided Option Key as a MessageChannel value, or returns the
	 * default one if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            Nullable default value used in the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	@Nullable
	@Contract("_, !null -> !null")
	public MessageChannel optMessageChannel(@NotNull String key, @Nullable MessageChannel defaultValue)
	{
		return getOption(key, defaultValue, optionMapping -> optionMapping.getAsChannel().asGuildMessageChannel());
	}

	/**
	 * Gets the provided Option Key as an Attachment value, or returns
	 * {@code null} if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	@Nullable
	public Message.Attachment optAttachment(@NotNull String key)
	{
		return optAttachment(key, null);
	}

	/**
	 * Gets the provided Option Key as an Attachment value, or returns the
	 * default one if the option cannot be found.
	 *
	 * @param key
	 *            The option we want
	 * @param defaultValue
	 *            Nullable default value used in the absence of the option value
	 *
	 * @return The provided option, or the default value if the option is not
	 *         present
	 */
	@Nullable
	@Contract("_, !null -> !null")
	public Message.Attachment optAttachment(@NotNull String key, @Nullable Message.Attachment defaultValue)
	{
		return getOption(key, defaultValue, OptionMapping::getAsAttachment);
	}

	/**
	 * Will return if the provided key resolves into a provided Option for the
	 * SlashCommand.
	 *
	 * @param key
	 *            the option we want
	 *
	 * @return true if the option exists, false otherwise
	 */
	public boolean hasOption(@NotNull String key)
	{
		return getOption(key) != null;
	}

	/**
	 * Compares a provided {@link ChannelType} with the one this event occurred
	 * on,
	 * returning {@code true} if they are the same ChannelType.
	 *
	 * @param channelType
	 *            The ChannelType to compare
	 *
	 * @return {@code true} if the CommandEvent originated from a
	 *         {@link MessageChannel}
	 *         of the provided ChannelType, otherwise {@code false}.
	 */
	public boolean isFromType(ChannelType channelType)
	{
		return getChannelType() == channelType;
	}

	/**
	 * Gets the settings of the guild in which this command was run.
	 *
	 * @param <S> the type of the settings
	 * @return the settings, or {@code null} if either of the following conditions are met:
	 * <ul>
	 *     <li>this interaction didn't happen in a guild</li>
	 *     <li>the client's {@link GuildSettingsManager} is null</li>
	 *     <li>the {@link GuildSettingsManager} returned null settings for the guild</li>
	 * </ul>
	 */
	@Nullable
	public <S> S getGuildSettings()
	{
		if (!isFromGuild()) {
			return null;
		}
		final GuildSettingsManager<S> manager = getClient().getSettingsManager();
		if (manager == null)
		{
			return null;
		}
		return manager.getSettings(getGuild());
	}

	/**
	 * Gets the settings of the guild in which this command was run.
	 *
	 * @param settingsClazz the class of the settings
	 * @param <S> the type of the settings
	 * @return the settings, or {@code null} if either of the following conditions are met:
	 * <ul>
	 *     <li>this interaction didn't happen in a guild</li>
	 *      <li>the client's {@link GuildSettingsManager} is null</li>
	 *      <li>the {@link GuildSettingsManager} returned null settings for the guild</li>
	 *      <li>the {@link GuildSettingsManager} returned settings that are not assignable to the {@code settingsClazz}</li>
	 * </ul>
	 */
	@Nullable
	@SuppressWarnings("rawtypes")
	public <S> S getGuildSettings(Class<? extends S> settingsClazz)
	{
		if (!isFromGuild()) {
			return null;
		}
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
	}

	@Override
	public SlashCommandInteractionEvent getEvent()
	{
		return this;
	}

	@Override
	public User getAuthor()
	{
		return getUser();
	}
}
