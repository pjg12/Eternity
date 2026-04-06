package eternity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	private List<CostPair> costs = new ArrayList<>();
	
	DataAction () {
		this.name = "";
		this.type = "";
		this.affinity = "";
		this.atk = 0;
		this.bdmg = 0;
		this.tdmg = 0;
		this.dmgMulti = 0.0;
		this.al = 0;
		this.ranged = 0;
		this.actionType = "Standard";
		this.weapon = "";
		this.costs = new ArrayList<>();
	}

	DataAction (DataAction newAction) {
		this.character = newAction.getCharacter();
		this.name = newAction.getName();
		this.type = newAction.getType();
		this.affinity = newAction.getAffinity();
		this.atk = newAction.getAtk();
		this.bdmg = newAction.getBdmg();
		this.tdmg = newAction.getTdmg();
		this.dmgMulti = newAction.getDmgMulti();
		this.al = newAction.getAl();
		this.ranged = newAction.getRanged();
		this.actionType = newAction.getActionType();
		this.weapon = newAction.getWeapon();
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

	public List<CostPair> getCosts() {
		return Collections.unmodifiableList(costs);
	}

	public void setCosts(List<CostPair> costs) {
		this.costs = new ArrayList<>();
		if (costs == null) {
			return;
		}
		for (CostPair cost : costs) {
			if (cost != null) {
				this.costs.add(new CostPair(cost));
			}
		}
	}

	public static class CostPair {
		private String type;
		private double value;

		public CostPair(String type, double value) {
			this.type = type;
			this.value = value;
		}

		public CostPair(CostPair other) {
			this.type = other == null ? null : other.type;
			this.value = other == null ? 0 : other.value;
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
