package eternity;

public class DataAction {
	private CharData character;
	
	private String name;
	private String type;
	private String affinity;
	private int atk;
	private int bdmg;
	private int tdmg;
	private double dmgMulti;
	private int al;
	private int ranged;
	private String actionType;
	private String weapon;
	private java.util.List<CostPair> costs = new java.util.ArrayList<>();
	
	DataAction () {
		setName("");
		setType("");
		setAffinity("");
		setAtk(0);
		setBdmg(0);
		setTdmg(0);
		setDmgMulti(0.0);
		setAl(0);
		setRanged(0);
		setActionType("Standard");
	}

	DataAction (DataAction newAction) {
		setName(newAction.getName());
		setType(newAction.getType());
		setAffinity(newAction.getAffinity());
		setAtk(newAction.getAtk());
		setBdmg(newAction.getBdmg());
		setTdmg(newAction.getTdmg());
		setDmgMulti(newAction.getDmgMulti());
		setAl(newAction.getAl());
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

	public int getAtk() {
		return atk;
	}

	public void setAtk(int atk) {
		this.atk = atk;
	}

	public int getBdmg() {
		return bdmg;
	}

	public void setBdmg(int bdmg) {
		this.bdmg = bdmg;
	}

	public int getTdmg() {
		return tdmg;
	}

	public void setTdmg(int tdmg) {
		this.tdmg = tdmg;
	}

	public double getDmgMulti() {
		return dmgMulti;
	}

	public void setDmgMulti(double dmgMulti) {
		this.dmgMulti = dmgMulti;
	}

	public int getAl() {
		return al;
	}

	public void setAl(int al) {
		this.al = al;
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
