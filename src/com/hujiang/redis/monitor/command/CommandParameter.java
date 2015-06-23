package com.hujiang.redis.monitor.command;

public class CommandParameter {
	
	private Object[] params = new Object[Command.MAX_PARAM_SIZE];
	
	public void clear() {
		for (int i = 0; i < Command.MAX_PARAM_SIZE; ++ i) {
			this.params[i] = null;
		}
		
		this.pushIndex	= 0;
		this.popIndex	= 0;
	}
	
	private int pushIndex = 0;
	public void push(Object s) {
		if (this.pushIndex < Command.MAX_PARAM_SIZE) {
			params[this.pushIndex] = s;
			this.pushIndex ++;
		}
	}
	
	private int popIndex = 0;
	public String popString() {
		String result = null;
		if ((this.popIndex < Command.MAX_PARAM_SIZE) && (this.params[this.popIndex] instanceof String)) {
			result = (String) this.params[this.popIndex];
			this.popIndex ++;
		}
		return result;
	}
	public Integer popInteger() {
		Integer result = null;
		if ((this.popIndex < Command.MAX_PARAM_SIZE) && (this.params[this.popIndex] instanceof Integer)) {
			result = (Integer) this.params[this.popIndex];
			this.popIndex ++;
		}
		return result;
	}
	
	public String toString() {
		String result = "";
		for (int i = 0; i < Command.MAX_PARAM_SIZE; ++ i) {
			if (this.params[i] == null) {
				break;
			}
			else {
				if (i != 0) {
					result += " ";
				}
				result += this.params[i];
			}
		}
		return result;
	}

	public static void main(String[] args) {
		CommandParameter params = new CommandParameter();
		
		params.clear();
		params.push("Hello");
		params.push("Word");
		
		System.out.println(params.popString());
		System.out.println(params.popInteger());
		System.out.println(params.popString());
		
		System.out.println(params.toString());
		
		params.clear();
	}

}
