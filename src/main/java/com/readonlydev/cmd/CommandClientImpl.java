package com.readonlydev.cmd;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.readonlydev.cmd.Command.Category;
import com.readonlydev.common.utils.FixedSizeCache;
import com.readonlydev.common.utils.SafeIdUtil;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.internal.utils.Checks;

/**
 * An implementation of {@link com.readonlydev.cmd.CommandClient
 * CommandClient} to be used by a bot.
 * <p>
 * This is a listener usable with {@link net.dv8tion.jda.api.JDA JDA}, as it
 * implements {@link net.dv8tion.jda.api.hooks.EventListener EventListener} in
 * order to catch and use different kinds of
 * {@link net.dv8tion.jda.api.events.Event Event}s. The primary usage of this is
 * where the CommandClient implementation takes
 * {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent
 * MessageReceivedEvent}s, and automatically processes arguments, and provide
 * them to a {@link com.readonlydev.cmd.Command Command} for running and
 * execution.
 */
public class CommandClientImpl implements CommandClient, EventListener {
	private static final Logger LOG = LoggerFactory.getLogger(CommandClient.class);
	private static final String DEFAULT_PREFIX = "@mention";

	private final OffsetDateTime start;
	private final Activity activity;
	private final OnlineStatus status;
	private final String ownerId;
	private final String[] coOwnerIds;
	private final String prefix;
	private final String altprefix;
	private final String[] prefixes;
	private final Function<MessageReceivedEvent, String> prefixFunction;
	private final Function<MessageReceivedEvent, Boolean> commandPreProcessFunction;
	private final String serverInvite;
	private final HashMap<String, Integer> commandIndex;
	private final HashMap<String, Integer> slashCommandIndex;
	private final ArrayList<Command> commands;
	private final ArrayList<SlashCommand> slashCommands;
	private final ArrayList<String> slashCommandIds;
	private final String forcedGuildId;
	private final boolean manualUpsert;
	private final boolean embedAllReplies;
	private final String success;
	private final String warning;
	private final String error;
	private final HashMap<String, OffsetDateTime> cooldowns;
	private final HashMap<String, Integer> uses;
	private final FixedSizeCache<Long, Set<Message>> linkMap;
	private final boolean useHelp;
	private final boolean shutdownAutomatically;
	private final Consumer<CommandEvent> helpConsumer;
	private final String helpWord;
	private final ScheduledExecutorService executor;
	private final GuildSettingsManager<?> manager;

	private String textPrefix;
	private CommandListener listener = null;
	private int totalGuilds;

	public CommandClientImpl(String ownerId, String[] coOwnerIds, String prefix, String altprefix, String[] prefixes,
			Function<MessageReceivedEvent, String> prefixFunction,
			Function<MessageReceivedEvent, Boolean> commandPreProcessFunction, Activity activity, OnlineStatus status,
			String serverInvite, String success, String warning, String error, ArrayList<Command> commands,
			ArrayList<SlashCommand> slashCommands, String forcedGuildId, boolean manualUpsert, boolean embedAllReplies,
			boolean useHelp, boolean shutdownAutomatically, Consumer<CommandEvent> helpConsumer,
			String helpWord, ScheduledExecutorService executor, int linkedCacheSize, GuildSettingsManager<?> manager) {
		Checks.check(ownerId != null,
				"Owner ID was set null or not set! Please provide an User ID to register as the owner!");
		if (!SafeIdUtil.checkId(ownerId)) {
			LOG.warn(String.format("The provided Owner ID (%s) was found unsafe! Make sure ID is a non-negative long!",
					ownerId));
		}
		if (coOwnerIds != null) {
			for (String coOwnerId : coOwnerIds) {
				if (!SafeIdUtil.checkId(coOwnerId)) {
					LOG.warn(String.format(
							"The provided CoOwner ID (%s) was found unsafe! Make sure ID is a non-negative long!",
							coOwnerId));
				}
			}
		}
		this.start = OffsetDateTime.now();

		this.ownerId = ownerId;
		this.coOwnerIds = coOwnerIds;
		this.prefix = (prefix == null) || prefix.isEmpty() ? DEFAULT_PREFIX : prefix;
		this.altprefix = (altprefix == null) || altprefix.isEmpty() ? null : altprefix;

		this.prefixes = (prefixes == null) || (prefixes.length == 0) ? null : prefixes;
		if (this.prefixes != null) {
			Arrays.sort(this.prefixes, Comparator.reverseOrder());
		}
		this.prefixFunction = prefixFunction;
		this.commandPreProcessFunction = commandPreProcessFunction == null ? event -> true : commandPreProcessFunction;
		this.textPrefix = prefix;
		this.activity = activity;
		this.status = status;
		this.serverInvite = serverInvite;
		this.success = success == null ? "" : success;
		this.warning = warning == null ? "" : warning;
		this.error = error == null ? "" : error;
		this.commandIndex = new HashMap<>();
		this.slashCommandIndex = new HashMap<>();
		this.commands = new ArrayList<>();
		this.slashCommands = new ArrayList<>();
		this.slashCommandIds = new ArrayList<>();
		this.forcedGuildId = forcedGuildId;
		this.manualUpsert = manualUpsert;
		this.embedAllReplies = embedAllReplies;
		this.cooldowns = new HashMap<>();
		this.uses = new HashMap<>();
		this.linkMap = linkedCacheSize > 0 ? new FixedSizeCache<>(linkedCacheSize) : null;
		this.useHelp = useHelp;
		this.shutdownAutomatically = shutdownAutomatically;
		this.helpWord = helpWord == null ? "help" : helpWord;
		this.executor = executor == null ? Executors.newSingleThreadScheduledExecutor() : executor;
		this.manager = manager;
		this.helpConsumer = helpConsumer == null ? (event) -> {
			StringBuilder builder = new StringBuilder("**" + event.getSelfUser().getName() + "** commands:\n");
			Category category = null;
			for (Command command : commands) {
				if (!command.isHidden() && (!command.isOwnerCommand() || event.isOwner())) {
					if (!Objects.equals(category, command.getCategory())) {
						category = command.getCategory();
						builder.append("\n\n  __").append(category == null ? "No Category" : category.getName())
						.append("__:\n");
					}
					builder.append("\n`").append(textPrefix).append(prefix == null ? " " : "").append(command.getName())
					.append(command.getArguments() == null ? "`" : " " + command.getArguments() + "`")
					.append(" - ").append(command.getHelp());
				}
			}
			User owner = event.getJDA().getUserById(ownerId);
			if (owner != null) {
				builder.append("\n\nFor additional help, contact **").append(owner.getName()).append("**#")
				.append(owner.getDiscriminator());
				if (serverInvite != null) {
					builder.append(" or join ").append(serverInvite);
				}
			}
			event.replyInDm(builder.toString(), unused -> {
				if (event.isFromType(ChannelType.TEXT)) {
					event.reactSuccess();
				}
			}, t -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
		} : helpConsumer;

		// Load commands
		for (Command command : commands) {
			addCommand(command);
		}
		// Load slash commands
		for (SlashCommand command : slashCommands) {
			addSlashCommand(command);
		}
	}

	@Override
	public void setListener(CommandListener listener) {
		this.listener = listener;
	}

	@Override
	public CommandListener getListener() {
		return listener;
	}

	@Override
	public List<Command> getCommands() {
		return commands;
	}

	@Override
	public List<SlashCommand> getSlashCommands() {
		return slashCommands;
	}

	@Override
	public boolean makeAllRepliesEmbeded() {
		return embedAllReplies;
	}

	@Override
	public boolean isManualUpsert() {
		return manualUpsert;
	}

	@Override
	public String forcedGuildId() {
		return forcedGuildId;
	}

	@Override
	public OffsetDateTime getStartTime() {
		return start;
	}

	@Override
	public OffsetDateTime getCooldown(String name) {
		return cooldowns.get(name);
	}

	@Override
	public int getRemainingCooldown(String name) {
		if (cooldowns.containsKey(name)) {
			int time = (int) Math.ceil(OffsetDateTime.now().until(cooldowns.get(name), ChronoUnit.MILLIS) / 1000D);
			if (time <= 0) {
				cooldowns.remove(name);
				return 0;
			}
			return time;
		}
		return 0;
	}

	@Override
	public void applyCooldown(String name, int seconds) {
		cooldowns.put(name, OffsetDateTime.now().plusSeconds(seconds));
	}

	@Override
	public void cleanCooldowns() {
		OffsetDateTime now = OffsetDateTime.now();
		cooldowns.keySet().stream().filter((str) -> (cooldowns.get(str).isBefore(now))).collect(Collectors.toList())
		.forEach(cooldowns::remove);
	}

	@Override
	public int getCommandUses(Command command) {
		return getCommandUses(command.getName());
	}

	@Override
	public int getCommandUses(String name) {
		return uses.getOrDefault(name, 0);
	}

	@Override
	public void addCommand(Command command) {
		addCommand(command, commands.size());
	}

	@Override
	public void addCommand(Command command, int index) {
		if ((index > commands.size()) || (index < 0)) {
			throw new ArrayIndexOutOfBoundsException(
					"Index specified is invalid: [" + index + "/" + commands.size() + "]");
		}
		synchronized (commandIndex) {
			String name = command.getName().toLowerCase(Locale.ROOT);
			// check for collision
			if (commandIndex.containsKey(name)) {
				throw new IllegalArgumentException(
						"Command added has a name or alias that has already been indexed: \"" + name + "\"!");
			}
			for (String alias : command.getAliases()) {
				if (commandIndex.containsKey(alias.toLowerCase(Locale.ROOT))) {
					throw new IllegalArgumentException(
							"Command added has a name or alias that has already been indexed: \"" + alias + "\"!");
				}
			}
			// shift if not append
			if (index < commands.size()) {
				commandIndex.entrySet().stream().filter(entry -> entry.getValue() >= index).collect(Collectors.toList())
				.forEach(entry -> commandIndex.put(entry.getKey(), entry.getValue() + 1));
			}
			// add
			commandIndex.put(name, index);
			for (String alias : command.getAliases()) {
				commandIndex.put(alias.toLowerCase(Locale.ROOT), index);
			}
		}
		commands.add(index, command);
	}

	@Override
	public void addSlashCommand(SlashCommand command) {
		addSlashCommand(command, slashCommands.size());
	}

	@Override
	public void addSlashCommand(SlashCommand command, int index) {
		if ((index > slashCommands.size()) || (index < 0)) {
			throw new ArrayIndexOutOfBoundsException(
					"Index specified is invalid: [" + index + "/" + slashCommands.size() + "]");
		}
		synchronized (slashCommandIndex) {
			String name = command.getName().toLowerCase(Locale.ROOT);
			// check for collision
			if (slashCommandIndex.containsKey(name)) {
				throw new IllegalArgumentException(
						"Command added has a name that has already been indexed: \"" + name + "\"!");
			}
			// shift if not append
			if (index < slashCommands.size()) {
				slashCommandIndex.entrySet().stream().filter(entry -> entry.getValue() >= index)
				.collect(Collectors.toList())
				.forEach(entry -> slashCommandIndex.put(entry.getKey(), entry.getValue() + 1));
			}
			// add
			slashCommandIndex.put(name, index);
		}
		slashCommands.add(index, command);
	}

	@Override
	public void removeCommand(String name) {
		synchronized (commandIndex) {
			if (!commandIndex.containsKey(name.toLowerCase(Locale.ROOT))) {
				throw new IllegalArgumentException("Name provided is not indexed: \"" + name + "\"!");
			}
			int targetIndex = commandIndex.remove(name.toLowerCase(Locale.ROOT));
			Command removedCommand = commands.remove(targetIndex);
			for (String alias : removedCommand.getAliases()) {
				commandIndex.remove(alias.toLowerCase(Locale.ROOT));
			}
			commandIndex.entrySet().stream().filter(entry -> entry.getValue() > targetIndex)
			.collect(Collectors.toList())
			.forEach(entry -> commandIndex.put(entry.getKey(), entry.getValue() - 1));
		}
	}

	@Override
	public String getOwnerId() {
		return ownerId;
	}

	@Override
	public long getOwnerIdLong() {
		return Long.parseLong(ownerId);
	}

	@Override
	public String[] getCoOwnerIds() {
		return coOwnerIds;
	}

	@Override
	public long[] getCoOwnerIdsLong() {
		if (coOwnerIds == null) {
			return null;
		}
		long[] ids = new long[coOwnerIds.length];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = Long.parseLong(coOwnerIds[i]);
		}
		return ids;
	}

	@Override
	public String getSuccess() {
		return success;
	}

	@Override
	public String getWarning() {
		return warning;
	}

	@Override
	public String getError() {
		return error;
	}

	@Override
	public ScheduledExecutorService getScheduleExecutor() {
		return executor;
	}

	@Override
	public String getServerInvite() {
		return serverInvite;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public String[] getPrefixes() {
		return prefixes;
	}

	@Override
	public Function<MessageReceivedEvent, String> getPrefixFunction() {
		return prefixFunction;
	}

	@Override
	public String getAltPrefix() {
		return altprefix;
	}

	@Override
	public String getTextualPrefix() {
		return textPrefix;
	}

	@Override
	public int getTotalGuilds() {
		return totalGuilds;
	}

	@Override
	public String getHelpWord() {
		return helpWord;
	}

	@Override
	public boolean usesLinkedDeletion() {
		return linkMap != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> S getSettingsFor(Guild guild) {
		if (manager == null) {
			return null;
		}
		return (S) manager.getSettings(guild);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <M extends GuildSettingsManager<?>> M getSettingsManager() {
		return (M) manager;
	}

	@Override
	public void shutdown() {
		GuildSettingsManager<?> manager = getSettingsManager();
		if (manager != null) {
			manager.shutdown();
		}
		executor.shutdown();
	}

	@Override
	public void onEvent(GenericEvent event) {
		if (event instanceof MessageReceivedEvent) {
			onMessageReceived((MessageReceivedEvent) event);
		} else if (event instanceof SlashCommandEvent) {
			onSlashCommand((SlashCommandEvent) event);
		} else if ((event instanceof MessageDeleteEvent) && usesLinkedDeletion()) {
			onMessageDelete((MessageDeleteEvent) event);
		} else if (event instanceof ReadyEvent) {
			onReady((ReadyEvent) event);
		} else if (event instanceof ShutdownEvent) {
			if (shutdownAutomatically) {
				shutdown();
			}
		}
	}

	private void onReady(ReadyEvent event) {
		if (!event.getJDA().getSelfUser().isBot()) {
			LOG.error("JDATools does not support CLIENT accounts.");
			event.getJDA().shutdown();
			return;
		}
		textPrefix = prefix.equals(DEFAULT_PREFIX) ? "@" + event.getJDA().getSelfUser().getName() + " " : prefix;
		if (activity != null) {
			event.getJDA().getPresence().setPresence(status == null ? OnlineStatus.ONLINE : status,
					"default".equals(activity.getName()) ? Activity.playing("Type " + textPrefix + helpWord)
							: activity);
		}
		// Start SettingsManager if necessary
		GuildSettingsManager<?> manager = getSettingsManager();
		if (manager != null) {
			manager.init();
		}
		// Upsert slash commands, if not manual
		if (!manualUpsert) {
			for (SlashCommand command : slashCommands) {
				CommandData data = command.buildCommandData();
				if ((forcedGuildId != null) || command.isGuildOnly()) {
					String guildId = forcedGuildId != null ? forcedGuildId : null;
					Guild guild = event.getJDA().getGuildById(guildId);
					if (guild == null) {
						LOG.error(
								"Could not find guild with specified ID: " + forcedGuildId + ". Not going to upsert.");
						continue;
					}
					List<CommandPrivilege> privileges = command.buildPrivileges(this);
					guild.upsertCommand(data).queue(command1 -> {
						slashCommandIds.add(command1.getId());
						if (!privileges.isEmpty()) {
							command1.updatePrivileges(guild, privileges).queue();
						}
					});
				} else {
					event.getJDA().upsertCommand(data).queue(command1 -> slashCommandIds.add(command1.getId()));
				}
			}
		}
	}

	private void onMessageReceived(MessageReceivedEvent event) {
		// Return if it's a bot
		if (event.getAuthor().isBot()) {
			return;
		}
		final MessageParts parts = getParts(event);
		if (parts != null) // starts with valid prefix
		{
			if (useHelp && parts.command.equalsIgnoreCase(helpWord)) {
				CommandEvent cevent = new CommandEvent(event, parts.prefixUsed, parts.args, this);
				if (listener != null) {
					listener.onCommand(cevent, null);
				}
				helpConsumer.accept(cevent); // Fire help consumer
				if (listener != null) {
					listener.onCompletedCommand(cevent, null);
				}
				return; // Help Consumer is done
			} else if (event.isFromType(ChannelType.PRIVATE) || event.getTextChannel().canTalk()) {
				String name = parts.command;
				String args = parts.args;
				final Command command; // this will be null if it's not a command
				synchronized (commandIndex) {
					int i = commandIndex.getOrDefault(name.toLowerCase(Locale.ROOT), -1);
					command = i != -1 ? commands.get(i) : null;
				}
				if (command != null) {
					CommandEvent cevent = new CommandEvent(event, parts.prefixUsed, args, this);
					if (listener != null) {
						listener.onCommand(cevent, command);
					}
					uses.put(command.getName(), uses.getOrDefault(command.getName(), 0) + 1);
					if (commandPreProcessFunction.apply(event)) {
						command.run(cevent);
					}
					return; // Command is done
				}
			}
		}
		if (listener != null) {
			listener.onNonCommandMessage(event);
		}
	}

	@Nullable
	private MessageParts getParts(MessageReceivedEvent event) {
		String rawContent = event.getMessage().getContentRaw();

		GuildSettingsProvider settings = event.isFromType(ChannelType.TEXT) ? provideSettings(event.getGuild()) : null;

		// Check for prefix or alternate prefix (@mention cases)
		if (prefix.equals(DEFAULT_PREFIX) || ((altprefix != null) && altprefix.equals(DEFAULT_PREFIX))) {
			if (rawContent.startsWith("<@" + event.getJDA().getSelfUser().getId() + ">")
					|| rawContent.startsWith("<@!" + event.getJDA().getSelfUser().getId() + ">")) {
				final int prefixLength = rawContent.indexOf('>') + 1;
				return makeMessageParts(rawContent, prefixLength);
			}
		}
		// Check for prefix
		// Run Function check if there is one, then fallback to normal prefixes
		if (prefixFunction != null) {
			String prefix = prefixFunction.apply(event);
			// Don't lowercase, up to Function to handle this
			if ((prefix != null) && rawContent.startsWith(prefix)) {
				final int prefixLength = prefix.length();
				return makeMessageParts(rawContent, prefixLength);
			}
		}
		final String lowerCaseContent = rawContent.toLowerCase(Locale.ROOT);
		// Check for default prefix
		if (lowerCaseContent.startsWith(prefix.toLowerCase(Locale.ROOT))) {
			final int prefixLength = prefix.length();
			return makeMessageParts(rawContent, prefixLength);
		}
		// Check for alternate prefix
		if ((altprefix != null) && lowerCaseContent.startsWith(altprefix.toLowerCase(Locale.ROOT))) {
			final int prefixLength = altprefix.length();
			return makeMessageParts(rawContent, prefixLength);
		}
		// Check for prefixes
		if (prefixes != null) {
			for (String pre : prefixes) {
				if (lowerCaseContent.startsWith(pre.toLowerCase(Locale.ROOT))) {
					final int prefixLength = pre.length();
					return makeMessageParts(rawContent, prefixLength);
				}
			}
		}
		// Check for guild specific prefixes
		if (settings != null) {
			Collection<String> prefixes = settings.getPrefixes();
			if (prefixes != null) {
				for (String prefix : prefixes) {
					if (lowerCaseContent.startsWith(prefix.toLowerCase(Locale.ROOT))) {
						final int prefixLength = prefix.length();
						return makeMessageParts(rawContent, prefixLength);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Processes the message raw content and returns the "parts" of the message <br>
	 * These parts include:
	 * <ul>
	 * <li>Used prefix</li>
	 * <li>Command name</li>
	 * <li>Arguments</li>
	 * </ul>
	 *
	 * @param rawContent The raw content of the incoming message
	 * @param prefixLength The length of the prefix that has been successfully
	 * detected before calling this method
	 *
	 * @return A MessageParts objects with all the parts cited above
	 */
	@NotNull
	private CommandClientImpl.MessageParts makeMessageParts(String rawContent, int prefixLength) {
		// Replacement method below
		// final String[] split = rawContent.substring(prefixLength).trim().split("\\s+", 2);

		// What we do is search for the first whitespace after the prefix, this gets us
		// the command name
		// To then get the arguments, we find the first occurrence of a character other
		// than a whitespace, after the command index, and take the string from that
		// index
		String cmd = null;
		for (int i = prefixLength; i < rawContent.length(); i++) {
			if (Character.isWhitespace(rawContent.charAt(i))) { // If a whitespace assume we found the end of the
				// command name
				cmd = rawContent.substring(prefixLength, i);
				break;
			}
		}
		String args = "";
		if (cmd == null) { // Assume there are no args since there were absolutely no whitespace
			cmd = rawContent.substring(prefixLength);
		} else {
			for (int i = prefixLength + cmd.length(); i < rawContent.length(); i++) {
				if (!Character.isWhitespace(rawContent.charAt(i))) { // If not a whitespace assume we found the start of
					// the arguments
					args = rawContent.substring(i);
					break;
				}
			}
		}
		// Just in case something fucked up
		LOG.trace("Received command named '{}' with args '{}'", cmd, args);

		return new MessageParts(rawContent.substring(0, prefixLength), cmd, args);
	}

	private void onSlashCommand(SlashCommandEvent event) {
		final SlashCommand command; // this will be null if it's not a command
		synchronized (slashCommandIndex) {
			int i = slashCommandIndex.getOrDefault(event.getName().toLowerCase(Locale.ROOT), -1);
			command = i != -1 ? slashCommands.get(i) : null;
		}
		if (command != null) {
			if (listener != null) {
				listener.onSlashCommand(event, command);
			}
			uses.put(command.getName(), uses.getOrDefault(command.getName(), 0) + 1);
			command.run(event, this);
			// Command is done
		}
	}

	private void onMessageDelete(MessageDeleteEvent event) {
		// We don't need to cover whether or not this client usesLinkedDeletion()
		// because
		// that is checked in onEvent(Event) before this is even called.
		synchronized (linkMap) {
			if (linkMap.contains(event.getMessageIdLong())) {
				Set<Message> messages = linkMap.get(event.getMessageIdLong());
				if ((messages.size() > 1) && event.getGuild().getSelfMember().hasPermission(event.getGuildChannel(),
						Permission.MESSAGE_MANAGE)) {
					event.getGuildChannel().deleteMessages(messages).queue(unused -> {
					}, ignored -> {
					});
				} else if (messages.size() > 0) {
					messages.forEach(m -> m.delete().queue(unused -> {
					}, ignored -> {
					}));
				}
			}
		}
	}

	private GuildSettingsProvider provideSettings(Guild guild) {
		Object settings = getSettingsFor(guild);
		if (settings instanceof GuildSettingsProvider) {
			return (GuildSettingsProvider) settings;
		} else {
			return null;
		}
	}

	void linkIds(long callId, Message message) {
		// We don't use linked deletion, so we don't do anything.
		if (!usesLinkedDeletion()) {
			return;
		}
		synchronized (linkMap) {
			Set<Message> stored = linkMap.get(callId);
			if (stored != null) {
				stored.add(message);
			} else {
				stored = new HashSet<>();
				stored.add(message);
				linkMap.add(callId, stored);
			}
		}
	}

	private static class MessageParts {
		private final String prefixUsed;
		private final String command;
		private final String args;

		private MessageParts(String prefixUsed, String command, String args) {
			this.prefixUsed = prefixUsed;
			this.command = command;
			this.args = args;
		}
	}
}