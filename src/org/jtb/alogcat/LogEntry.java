package org.jtb.alogcat;

public class LogEntry {
	private Level level = Level.V;
	private String text = null;
	private Integer hash = null;

	public LogEntry(String text, Level level) {
		this.text = text;
		this.level = level;
	}

	public Level getLevel() {
		return level;
	}

	public String getText() {
		return text;
	}

	@Override
	public int hashCode() {
		if (hash == null) {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((level == null) ? 0 : level.hashCode());
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			hash = result;
		}

		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogEntry other = (LogEntry) obj;
		if (level != other.level)
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}
