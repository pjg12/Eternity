package eternity;

public class DataAction {
	private CharData character;
	
	private String name;
	private String type;
	private String affinity;
	private int ranged;
	private String actionType;
	private String weapon;
	private java.util.List<CostPair> costs = new java.util.ArrayList<>();
	
	DataAction () {
		setName("");
		setType("");
		setAffinity("");
		setRanged(0);
		setActionType("Standard");
	}

	DataAction (DataAction newAction) {
		setName(newAction.getName());
		setType(newAction.getType());
		setAffinity(newAction.getAffinity());
		setRanged(newAction.getRanged());
		setActionType(newAction.getActionType());
		setCharacter(newAction.getCharacter());
		setCosts(newAction.getCosts());
	}

	public CharData getCharacter() {
		return character;
	}

	public void setCharacter(CharData character) {
		this.character = character;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAffinity() {
		return affinity;
	}

	public void setAffinity(String affinity) {
		this.affinity = affinity;
	}

	public int getRanged() {
		return ranged;
	}

	public void setRanged(int ranged) {
		this.ranged = ranged;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getWeapon() {
		return weapon;
	}

	public void setWeapon(String weapon) {
		this.weapon = weapon;
	}

	public java.util.List<CostPair> getCosts() {
		return costs;
	}

	public void setCosts(java.util.List<CostPair> costs) {
		this.costs = costs;
	}

	public static class CostPair {
		private String type;
		private double value;

		public CostPair(String type, double value) {
			this.type = type;
			this.value = value;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}
	}
	
	
	
}
