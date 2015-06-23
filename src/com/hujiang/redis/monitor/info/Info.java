package com.hujiang.redis.monitor.info;

public abstract class Info {
	
	protected String s	= null;
	protected int index	= 0;
	protected int len	= 0;
	
	abstract public void clear();
	abstract public String toJSON();
	abstract public boolean process(final String s);
	
	public boolean preprocess(final String s) {
		if (s == null || s.isEmpty()) {
			return false;
		}
		
		this.s		= s;
		this.index	= 0;
		this.len	= s.length();
		
		return true;
	}
	
	protected void skipLineBreak() {
		while (this.index < this.len && this.isLineBreak(s.charAt(this.index))) {
			this.index ++;
		}
		
		// Skip the '\n' character.
		if (this.index < this.len) {
			index ++;
		}
	}
	
	protected void skipALine() {
		while (this.index < this.len) {
			if (this.s.charAt(this.index) == '\n') {
				break;
			}
			else {
				this.index ++;
			}
		}
		
		// Skip the '\n' character.
		if (this.index < this.len) {
			index ++;
		}
	}
	
	protected String getNextValue() {
		// Skip the value name.
		while (!(this.isColon(this.s.charAt(this.index)))) {
			this.index ++;
		}
		
		this.index ++;
		int begin = this.index;
		
		// Get the value body.
		while (this.index < this.len) {
			if (this.isLineBreak(this.s.charAt(this.index))) {
				break;
			}
			else {
				this.index ++;
			}
		}
		
		return this.s.substring(begin, this.index);
	}
	
	private boolean isSeparator(char c) {
		return (c == ' ' || c == '-' || this.isColon(c) || this.isLineBreak(c));
	}
	
	private boolean isLineBreak(char c) {
		return (c == '\r' || c == '\n');
	}
	
	private boolean isColon(char c) {
		return (c == ':');
	}
	
	protected String getNextToken() {
		// Skip blank and other characters.
		char c = 0;
		while (this.index < this.len) {
			c = this.s.charAt(this.index);
			if (this.isSeparator(c)) {
				this.index ++;
			}
			else {
				break;
			}
		}
		if (this.index >= this.len) {
			return null;
		}
		
		int begin = this.index;
		while (this.index < this.len) {
			c = this.s.charAt(this.index);
			if (this.isSeparator(c)) {
				break;
			}
			else {
				this.index ++;
			}
		}
		return this.s.substring(begin, this.index);
	}
}
