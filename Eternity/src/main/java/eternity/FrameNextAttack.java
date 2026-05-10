package eternity;

/**
 * Wrapper frame for next-attack style actions.
 * Mirrors FrameAttack behavior while allowing separate call sites/types.
 */
public class FrameNextAttack extends FrameAttack {
	private static final long serialVersionUID = 1L;

	FrameNextAttack(FrameSheet sheetFrame, FrameCombat combatFrame, CharData character, DataAction action) {
		super(sheetFrame, combatFrame, character, action);
		setTitle("Next Attack Helper");
	}
}
