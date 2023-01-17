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

package com.readonlydev.command.client;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.readonlydev.command.ctx.ContextMenu;
import com.readonlydev.command.slash.SlashCommand;
import com.readonlydev.common.utils.SafeIdUtil;

public final class ServerCommands
{
	private final long			serverId;
	private List<SlashCommand>	slashCommands = new LinkedList<>();
	private List<ContextMenu>	contextMenus = new LinkedList<>();

	public ServerCommands(long serverId)
	{
		this.serverId = serverId;
	}

	public ServerCommands(String serverId)
	{
		this(SafeIdUtil.safeConvert(serverId));
	}

	public ServerCommands addAllCommands(Collection<SlashCommand> commandCollection)
	{
		for(SlashCommand cmd : commandCollection)
		{
			cmd.setGuildId(this.serverId);
			this.slashCommands.add(cmd);
		}
		return this;
	}

	long getServerId()
	{
		return serverId;
	}

	List<SlashCommand> getSlashCommands()
	{
		return slashCommands;
	}

	List<ContextMenu> getContextMenus()
	{
		return contextMenus;
	}
}
