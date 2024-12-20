package GameTest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Player {
    private int x, y, width, height, speed;
    private int health;
     boolean isJumping, isAttacking, isBlocking;
    private boolean isKnockedBack;
    private int knockbackDirection;
    private int knockbackTime;
    private int velocityY;
    private Platform platform;
    private boolean isPlayerOne;
    private boolean isInAttackRange;
    private int swordLength = 50;  // Length of the sword
    private int swordWidth = 10;   // Width of the sword
    private boolean isDead = false;
    private String lastDirection = "right";  // Track the last direction ("left" or "right")
    private int respawnX;
    private int respawnY;
    private boolean isMovingLeft = false; // Declare and initialize the variable
    private boolean isMovingRight = false; // Declare and initialize the variable
    private double velocityX = 0;  // Smooth horizontal movement
    private BufferedImage idleSprite, runningLeftSprite, runningRightSprite, jumpingSprite, attackLeftSprite, attackRightSprite;
    
    public Player(int x, int y, Platform platform, boolean isPlayerOne) {
        this.x = x;
        this.y = y;
        this.width = 50;
        this.height = 100;
        this.speed = 5;
        this.health = 100;
        this.platform = platform;
        this.isPlayerOne = isPlayerOne;
        this.respawnX = x; // Store the initial respawn position
        this.respawnY = y;
        this.isInAttackRange = false;
    }
    
    private void loadSprites() {
        try {
            idleSprite = ImageIO.read(getClass().getResource("/Sprite_Idle.png"));
            runningLeftSprite = ImageIO.read(getClass().getResource("/Sprite_Running_Left.png"));
            runningRightSprite = ImageIO.read(getClass().getResource("/Sprite_Running_Right.png"));
            jumpingSprite = ImageIO.read(getClass().getResource("/Sprite_Jumping.png"));
            attackLeftSprite = ImageIO.read(getClass().getResource("/Sprite_StrikeOnly(LEFT).png"));
            attackRightSprite = ImageIO.read(getClass().getResource("/Sprite_StrikeOnly(RIGHT).png.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading sprites.");
        }
    }

    public void update() {
        if (isDead) {
            return;  // Don't update the player if they are dead
        }

        if (isKnockedBack) {
            x += knockbackDirection * 10;
            knockbackTime--;
            if (knockbackTime <= 0) {
                isKnockedBack = false;
            }
        } else {
            // Handle gravity and jumping
            if (isJumping) {
                velocityY = -15;
                isJumping = false;
            }
            y += velocityY;
            velocityY += 1;
            if (y >= platform.getY()) {
                y = platform.getY();
                velocityY = 0;
            }

            // Smooth horizontal movement
            if (isMovingLeft) {
                velocityX = Math.max(velocityX - 0.5, -speed); // Accelerate to the left
                lastDirection = "left";  // Update the last direction
            } else if (isMovingRight) {
                velocityX = Math.min(velocityX + 0.5, speed); // Accelerate to the right
                lastDirection = "right";  // Update the last direction
            } else {
                // Gradually slow down when no key is pressed
                velocityX = Math.signum(velocityX) * Math.max(0, Math.abs(velocityX) - 0.5);
            }
            x += velocityX; // Apply the smooth movement
        }
    }
    
    


    public void attack(Player opponent) {
        if (isDead) return;  // Don't allow attacking if dead
        isAttacking = true;

        // Check if the attack hits the opponent and if they are not blocking
        if (isSwordCollidingWith(opponent)) {
            if (opponent.isBlocking) {
                // If opponent is blocking, apply knockback in opposite direction
                opponent.knockbackDirection = (x > opponent.x) ? -1 : 1;
                opponent.isKnockedBack = true;
                opponent.knockbackTime = 10;
                System.out.println("Attack blocked! Knockback applied to " + (opponent.isPlayerOne ? "Player 1" : "Player 2"));
            } else {
                // If not blocking, apply damage and knockback
                opponent.takeDamage(10);
                opponent.knockbackDirection = (x > opponent.y) ? 1 : -1;
                opponent.isKnockedBack = true;
                opponent.knockbackTime = 10;
            }
        }
    }

    public void block() {
        if (isDead) return;  // Don't allow blocking if dead
        isBlocking = true;  // Set the player as blocking
    }


    public void moveLeft() {
        if (!isDead && !isKnockedBack) {
            velocityX = -speed;
            lastDirection = "left";  // Update the last direction
        }
    }

    public void moveRight() {
        if (!isDead && !isKnockedBack) {
            velocityX = speed;
            lastDirection = "right";  // Update the last direction
        }
    }

    public void jump() {
        if (y == platform.getY() && !isDead) {
            isJumping = true;
        }
    }

    public void stop() {
        if (isDead) return;  // Don't stop if dead
        isAttacking = false;
        isBlocking = false;
        velocityX = 0;
    }

    private boolean isSwordCollidingWith(Player opponent) {
        // Check if the sword intersects the opponent's body
        int swordX = lastDirection.equals("right") ? x + width : x - swordLength;
        int swordY = y + height / 3;

        return swordX < opponent.x + opponent.width &&
               swordX + swordLength > opponent.x &&
               swordY < opponent.y + opponent.height &&
               swordY + swordWidth > opponent.y;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            die();
        }
    }

    private void die() {
        isDead = true;
        // Optionally, play death animation here or trigger round end
    }


    public void respawn() {
        x = respawnX;
        y = respawnY;
        health = 100;
        isDead = false;
        isJumping = false;
        isAttacking = false;
        isBlocking = false;
        velocityY = 0;
        velocityX = 0;
    }

    public void render(Graphics g) {
        if (isDead) return;  // Don't render the player if dead
            BufferedImage spriteToDraw = idleSprite;

            if (isAttacking) {
                spriteToDraw = lastDirection.equals("right") ? attackRightSprite : attackLeftSprite;
            } else if (isJumping) {
                spriteToDraw = jumpingSprite;
            } else if (isMovingLeft) {
                spriteToDraw = runningLeftSprite;
            } else if (isMovingRight) {
                spriteToDraw = runningRightSprite;
            }

            g.drawImage(spriteToDraw, x, y - height, width, height, null);
            
            g.setColor(Color.BLACK);
            g.fillRect(x, y - height -10, width, 110);


            // Render Health Bar
            g.setColor(Color.BLACK);
            g.fillRect(x, y - height - 10, width, 10);
            g.setColor(Color.RED);
            g.fillRect(x, y - height - 10, (int) (width * (health / 100.0)), 10);
        
        // Render Shield if blocking
        if (isBlocking) {
            g.setColor(Color.ORANGE);
            if (lastDirection.equals("right")) {
                g.fillRect(x + width, y - 150 / 2, 20, 60);
            } else {
                g.fillRect(x - 20, y - 150 / 2, 20, 60);
            }
        }

        // Render Sword if attacking
        if (isAttacking) {
            g.setColor(Color.GRAY);
            if (lastDirection.equals("right")) {
                g.fillRect(x + width, y - height / 2, swordLength, swordWidth);
            } else {
                g.fillRect(x - swordLength, y - height / 2, swordLength, swordWidth);
            }
        }
    }

    public int getHealth() {
        return health;
    }

    public boolean isDead() {
        return isDead;
    }
}
