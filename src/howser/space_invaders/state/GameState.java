package howser.space_invaders.state;

import howser.space_invaders.InputHandler;
import howser.space_invaders.entity.EnemyShip;
import howser.space_invaders.entity.PlayerShip;
import howser.space_invaders.entity.SceneryEntity;
import howser.space_invaders.entity.Ship;
import howser.space_invaders.entity.ShotEntity;
import howser.space_invaders.gfx.Colour;
import howser.space_invaders.gfx.Font;
import howser.space_invaders.gfx.Frame;
import howser.space_invaders.gfx.Sprite;
import howser.space_invaders.gfx.SpriteSheet;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

public class GameState extends BaseState {

	private ArrayList<SceneryEntity> scenery;
	private ArrayList<ShotEntity> playerShots;
	private ArrayList<EnemyShip> enemyShips;
	private PlayerShip player;
	private SpriteSheet sprites;
	private InputHandler input;
	private boolean paused = false;
	private Font font;
	private int score;
	private Random rand;
	private final int STAR_ROW = 2;
	private final int MAX_STARS = 2;
	private final int PLANET_ROW = 3;
	private final int MAX_PLANETS = 5;
	private int width, height;

	public GameState(String name, StateManager stateManager,
			InputHandler input, int width, int height) {
		super(name, stateManager);
		sprites = new SpriteSheet("/sprite_sheet.png");
		font = new Font("/main_font.png");
		this.input = input;
		rand = new Random();
		this.width = width;
		this.height = height;
	}

	public void tick() {
		if (!paused) {

			for (int i = 0; i < playerShots.size(); i++) {
				playerShots.get(i).tick();
				if (playerShots.get(i).y < 0) {
					playerShots.remove(i);
					i--;
				}
			}
			
			for (int i = 0; i < enemyShips.size(); i++) {
					enemyShips.get(i).tick();
					for (int j = 0; j < playerShots.size(); j++){
						if (enemyShips.get(i).collides((int)playerShots.get(j).x, (int)playerShots.get(j).y, playerShots.get(j).width, playerShots.get(j).height)){
							enemyShips.get(i).die();
							playerShots.remove(j);
							j--;
						}
					}
				if (enemyShips.get(i).isToBeRemoved()) {
					enemyShips.remove(i);
					i--;
				}
			}

			player.tick();
			if (player.x < 0){
				player.x = 0;
			} else if (player.x + player.width > width){
				player.x = width-player.width;
			}

			for (int i = 0; i < scenery.size(); i++) {
				scenery.get(i).tick();
				if (scenery.get(i).y > height) {
					scenery.remove(i);
					i--;
				}
			}
			generateScenery();
			sortRenderDepth();
			generateEnemyShips();
		}
		if (input.keyPressedThisFrame(KeyEvent.VK_ESCAPE)) {
			paused = !paused;
		}
	}

	public void render(Frame frame) {
		frame.clear(Colour.BLACK);
		for (int i = 0; i < scenery.size(); i++) {
			scenery.get(i).render(frame);
		}

		player.render(frame);

		for (int i = 0; i < enemyShips.size(); i++) {
			enemyShips.get(i).render(frame);
		}
		
		for (ShotEntity s : playerShots) {
			s.render(frame);
		}

		if (paused) {
			frame.renderString("Game Paused", font,
					frame.getWidth() / 2 - 6 * 8, 100, Colour.WHITE);
		}
	}

	public void onEnter() {
		enemyShips = new ArrayList<EnemyShip>();
		scenery = new ArrayList<SceneryEntity>();
		playerShots = new ArrayList<ShotEntity>();
		player = new PlayerShip(
				Sprite.getSpriteFromSheet(sprites, 0, 0, 16, 16), width / 2,
				height - 30, 2, input, Sprite.getSpriteFromSheet(sprites, 0,
						16, 3, 3));
		player.setLists(playerShots, enemyShips);
		input.addKeyListen(KeyEvent.VK_ESCAPE);
	}

	public void onExit() {
		input.clearKeyListens();
		enemyShips.clear();
	}

	public void reset() {

	}

	public void generateScenery() {
		// give the thing a 20% chance to generate an object
		if (rand.nextInt(100) < 80) {
			return;
		}
		// Compute amount of items to generate
		int itemCount = rand.nextInt(7);
		// generate the items
		for (int i = 0; i < itemCount; i++) {
			// Create a star (90%)
			if (rand.nextInt(100) < 99) {
				int star = rand.nextInt(MAX_STARS);
				scenery.add(new SceneryEntity(Sprite.getSpriteFromSheet(
						sprites, star * 16, STAR_ROW * 16, 16, 16), rand
						.nextInt(256), (rand.nextFloat() + 0.1f)*0.5f, 1));
			} else {
				int planet = rand.nextInt(MAX_PLANETS);
				float distance = rand.nextFloat() + 0.2f * 4;
				scenery.add(new SceneryEntity(Sprite.getSpriteFromSheet(
						sprites, planet * 16, PLANET_ROW * 16, 16, 16), rand
						.nextInt(256), distance, distance + 1.0f));
			}
		}
	}
	
	public void generateEnemyShips(){
		if (rand.nextInt(100)<1){
			EnemyShip ship = new EnemyShip(Sprite.getSpriteFromSheet(sprites, 16, 0, 16, 16), rand.nextInt(width), -16, 0, 2, Colour.PURPLE, false, 50, width, height);
			enemyShips.add(ship);
		}
	}

	public void sortRenderDepth() {
		// lower render depth gets rendered first
		for (int i = 1; i < scenery.size() - 1; i++) {
			SceneryEntity se = scenery.get(i);
			int holePos = i;
			while (holePos > 0
					&& se.renderDepth < scenery.get(holePos - 1).renderDepth) {
				scenery.set(holePos, scenery.get(holePos - 1));
				holePos--;
			}
			scenery.set(holePos, se);
		}
	}
}
