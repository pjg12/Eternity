package eternity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * An Aura Tech Training entry represents the data required for the training portion of an aura tech.
 */
@JsonIgnoreProperties({ "nextAt" })
public class DataATTraining {
	private int id;
	private String name;
	private String affinity;
	private String description;
	private int rank;
	private double exp;
	private double levelMod;
	private ArrayList<Integer> prereqs;
	
	/*
	 * DEFAULT CONSTRUCTOR
	 */
	public DataATTraining () {
		id = -1;
		name = "";
		affinity = "";
		description = "";
		rank = 0;
		exp = 0.0;
		levelMod = 1.0;
		prereqs = new ArrayList<Integer>();
	} //END OF DEFAULT CONSTRUCTOR
	
	/*
	 * COPY CONSTRUCTOR
	 */
	public DataATTraining (DataATTraining inTrain) {
		setId(inTrain.getId());
		setName(inTrain.getName());
		setAffinity(inTrain.getAffinity());
		setDescription(inTrain.getDescription());
		setRank(inTrain.getRank());
		setExp(inTrain.getExp());
		setLevelMod(inTrain.getLevelMod());
		setPrereqs(inTrain.getPrereqs());
	} //END OF COPY CONSTRUCTOR

	
	
	
	
	
	/**
	 ** 
	 ** Computed Getters
	 **
	 **/
	/*
	 * maxRank - A Computed Value for the Maximum Rank Currently Available for the Aura Technique
	 */
	public int getMaxRank(CharData character) {
		if (character == null || character.getIdentity() == null) return 0;

		double tempDouble, tempNum, tempRank;
		
		if (levelMod > 1) {
			tempDouble = Math.floor(levelMod);
			tempNum = levelMod - tempDouble;
			if (tempNum == 0.0) {
				tempDouble--;
				tempNum++;
			}
		}
		if (levelMod < 0) {
			tempDouble = Math.ceil(levelMod);
			tempNum = levelMod - tempDouble + 1;
			if (tempNum == 0.0) {
				tempDouble--;
				tempNum++;
			}
		}
		else {
			tempDouble = 0.0;
			tempNum = levelMod;
		}
		tempRank = character.getIdentity().getLevel() * tempNum;
		
		List<String> natAff = character.getTraining().getNaturalAffinities();
		for (String a : natAff) {
			if (a.compareTo(affinity) == 0) {
				tempRank++;
				break;
			}
		}
		tempRank -= tempDouble;
		
		for (int i = 0; i < prereqs.size(); i++) {
			var prereqTech = character.getTraining().getTrainingById(prereqs.get(i));
			if (prereqTech != null) {
				tempNum = prereqTech.getRank();
				if (tempNum < tempRank) {
					tempRank = tempNum;
				}
				
			}
			else {
				tempRank = 0;
				break;
			}
		}
		return (int)tempRank;
	} //End of maxRank
	/*
	 * prereqCap - The prereq that is preventing further leveling.
	 */
	public String getPrereqCap(CharData character) {
		String tempString = "Level";
		if (character == null || character.getIdentity() == null) return tempString;

		double tempDouble, tempNum, tempRank;
		
		if (levelMod > 1) {
			tempDouble = Math.floor(levelMod);
			tempNum = levelMod - tempDouble;
			if (tempNum == 0.0) {
				tempDouble--;
				tempNum++;
			}
		}
		else {
			tempDouble = 0.0;
			tempNum = levelMod;
		}
		tempRank = character.getIdentity().getLevel() * tempNum;
		
		List<String> natAff = character.getTraining().getNaturalAffinities();
		for (String a : natAff) {
			if (a.compareTo(affinity) == 0) {
				tempRank++;
				break;
			}
		}
		tempRank -= tempDouble;
		
		for (int i = 0; i < prereqs.size(); i++) {
			var prereqTech = character.getTraining().getTrainingById(prereqs.get(i));
			if (prereqTech != null) {
				tempNum = prereqTech.getRank();
				if (tempNum < tempRank) {
					tempRank = tempNum;
					tempString = prereqTech.getName();
				}
				
			}
			else {
				tempString = "Prerequisite";
				tempRank = 0;
				break;
			}
		}
		return tempString;
	} //End of maxRank
	/*
	 * nextAt - A Computed Value for the Amount of Experience Required to Reach the Next Rank of the Aura Technique
	 */
	public int getNextAt(CharData character) {
		int tempInt = getRank()*4 + 10;
		if (affinity.compareTo("Spirit")==0 || affinity.compareTo("Time")==0) {
			tempInt = (int)(tempInt * 1.5);
		}
		if (character != null && character.getTraining() != null) {
			List<String> natAff = character.getTraining().getNaturalAffinities();
			for (String a : natAff) {
				if (a.compareTo(affinity) == 0) {
				tempInt /= 2;
				break;
				}
			}			
		}

		return tempInt;
	} //TODO
	
	
	
	
	
	
	/**
	 ** 
	 ** Getters & Setters
	 **
	 **/
	/*
	 * id - A Unique Identifier for the Aura Technique Training
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	} // END OF ID

	/*
	 * name - The Aura Technique Title
	 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	} // END OF name

	/*
	 * affinity - The Affinity of the Aura Technique
	 */
	public String getAffinity() {
		return affinity;
	}
	public void setAffinity(String affinity) {
		if (affinity.compareTo("") == 0) {
			this.affinity = "None";
		}
		else {
			this.affinity = affinity;
		}
	} // END OF affinity

	/*
	 * description - A Verbose Description of the Aura Technique
	 */
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	} // END OF description

	/*
	 * rank - The Current Rank of the Aura Technique
	 */
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	} // END OF rank

	/*
	 * exp - The Current Experience Toward the Next Rank of the Aura Technique
	 */
	public double getExp() {
		return exp;
	}
	public void setExp(double exp) {
		this.exp = exp;
	} // END OF exp
	
	/*
	 * levelMod - The Modifier to Level for the Prereqs of the Aura Technique
	 */
	public double getLevelMod() {
		return levelMod;
	}
	public void setLevelMod(double levelMod) {
		this.levelMod = levelMod;
	} // END OF levelMod

	/*
	 * prereqs - A List of the IDs for Prerequisite Aura Techniques to Improve the Aura Technique
	 */
	public ArrayList<Integer> getPrereqs() {
		return prereqs;
	}
	public void setPrereqs(ArrayList<Integer> prereqs) {
		this.prereqs = prereqs;
	}
} // END OF CLASS
