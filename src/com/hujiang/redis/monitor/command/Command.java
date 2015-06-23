package com.hujiang.redis.monitor.command;

public enum Command {
	
	UNKNOWN,
	INFO,
	MEET,		// IP:String, Port:String
	FLUSH,		// UUID:String
	FLUSH_ALL,
	MOVE;		// From_UUID:String, To_UUID:String, From_Slot:Integer, To_Slot:Integer
	
	public static int MAX_PARAM_SIZE = 5;
	
	public static Command parse(String s, CommandParameter params) {
		// Check the argument.
		if (s == null || s.isEmpty()) {
			return Command.UNKNOWN;
		}
		
		if (params != null) {
			params.clear();
		}
		
		// Transform the input string into upper case.
		String up = s.toLowerCase();
		int len = up.length();
		
		// Parse it.
		switch(up.charAt(0)) {
		case 'f':
			if ((len == 46) &&		// 5 + 40
					(up.charAt(1) == 'l') &&
					(up.charAt(2) == 'u') &&
					(up.charAt(3) == 's') &&
					(up.charAt(4) == 'h') &&
					(up.charAt(5) == ',')) {
				// FLUSH
				if (params == null) {
					return Command.UNKNOWN;
				}
				params.push(s.substring(6));
				return Command.FLUSH;
			}
			else if ((len == 8) &&
						(up.charAt(1) == 'l') &&
						(up.charAt(2) == 'u') &&
						(up.charAt(3) == 's') &&
						(up.charAt(4) == 'h') &&
						(up.charAt(5) == 'a') &&
						(up.charAt(6) == 'l') &&
						(up.charAt(7) == 'l')) {
				// FLUSHALL
				return Command.FLUSH_ALL;
			}
			break;
		case 'i':
			if ((len == 4) &&
					(up.charAt(1) == 'n') &&
					(up.charAt(2) == 'f') &&
					(up.charAt(3) == 'o')) {
				// INFO
				return Command.INFO;
			}
			break;
		case 'm':
			if ((len >= 86) &&
					(up.charAt(1) == 'o') &&
					(up.charAt(2) == 'v') &&
					(up.charAt(3) == 'e') &&
					(up.charAt(4) == ',')) {
				
				if (params == null) {
					return Command.UNKNOWN;
				}

				// From_UUID
				int i = 5;
				int start = 5;
				while ((i < len) && (up.charAt(i) != ',')) ++ i;
				if (i >= len) {
					return Command.UNKNOWN;
				}
				System.out.println(up.charAt(i));
				System.out.println(up.substring(start, i));
				params.push(up.substring(start, i)); 
				
				// From_UUID
				start = ++ i;
				while ((i < len) && (up.charAt(i) != ',')) ++ i;
				if (i >= len) {
					return Command.UNKNOWN;
				}
				params.push(up.substring(start, i)); 
				
				// From_Slot
				start = ++ i;
				while ((i < len) && (up.charAt(i) != ',')) ++ i;
				if (i >= len) {
					return Command.UNKNOWN;
				}
				params.push(Integer.parseInt(up.substring(start, i))); 
				
				// To_Slot
				start = ++ i;
				if (i >= len) {
					return Command.UNKNOWN;
				}
				params.push(Integer.parseInt(up.substring(start, len))); 
				
				return Command.MOVE;
			}
			else if ((len >= 14) &&
						(up.charAt(1) == 'e') &&
						(up.charAt(2) == 'e') &&
						(up.charAt(3) == 't') &&
						(up.charAt(4) == ',')) {
				
				if (params == null) {
					return Command.UNKNOWN;
				}
				
				// IP
				
				// Port
				
				return Command.MEET;
			}
			break;
		}
		
		return Command.UNKNOWN;
	}

	public static void main(String[] args) {
		CommandParameter param = new CommandParameter();
		
		Command cmd = Command.UNKNOWN;
		
		cmd = Command.parse("flush,cdefba9190afbec518f68c61fb58788bdc2ac574", param);
		System.out.println(cmd.name());
		System.out.println(param.popString());
		
		cmd = Command.parse("move,cdefba9190afbec518f68c61fb58788bdc2ac574,cdefba9190afbec518f68c61fb58788bdc2ac574,1,15", param);
		System.out.println(cmd.name());
		System.out.println(param.popString());
	}
}
