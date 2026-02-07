package ru.mikst74.mikstcraft.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.mikst74.mikstcraft.collision.CollisionDetector;
import ru.mikst74.mikstcraft.input.InputEventData;
import ru.mikst74.mikstcraft.main.CommunicationManager;
import ru.mikst74.mikstcraft.model.time.GameTick;
import ru.mikst74.mikstcraft.util.math.Hitbox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static ru.mikst74.mikstcraft.util.math.ExtMath.isZeroVector;


@Getter
public class BaseEntity {
    public static final  float    FRACTION_START          = 0.03f;
    public static final  float    FRACTION_STOP           = 0.97f;
    private static final float    ONE_VELOCITY_PER_SECOND = (1 / GameTick.TICKS_PER_SECOND);
    public static final  Vector3f UP_VECTOR               = new Vector3f(0, 1, 0);

    protected final CommunicationManager communicationManager;

    @Setter
    private CollisionDetector collisionDetector;


    // Current state fields
    protected Vector3f position                         = new Vector3f();
    /**
     * current velocity
     */
    protected Vector3f speedVelocity                    = new Vector3f();
    protected Vector3f velocity                         = new Vector3f();
    protected float    velocityGoingDirectionCoef;
    protected float[]  velocityGoingDirectionCoefMatrix = new float[]{1.0f, 1.0f, 0.707f, 0.499f};

    /**
     * Current angles of view
     * angle.x - yaw (left/right angle).
     * angle.y - pitch; (up/down angle)
     * angle.z - roll наклон
     */
    protected Vector3f angle = new Vector3f();
    /**
     * States: walk, fly, swim, sneak,squat
     */
    protected boolean  isSneak;
    protected boolean  isSquat;
    protected boolean  isFly;
    protected boolean  isSwim;
    /**
     * States of moving directions
     */
    protected boolean  isGoing;
    protected boolean  isGoingForward;
    protected boolean  isGoingBack;
    protected boolean  isGoingRight;
    protected boolean  isGoingLeft;
    protected boolean  isGoingUp;
    protected boolean  isGoingDown;

    // entity characteristics fields
    protected final Hitbox hitbox = new Hitbox();
    protected       float  normalSpeed;
    protected       float  sneakSpeed;
    protected       float  squatSpeed;
    protected       float  flySpeed;
    protected       float  swimSpeed;

    // Calculated fields
    protected final Vector3f direction  = new Vector3f(0, 3, 0).normalize();
    protected final Vector3f directionH = new Vector3f(0, 3, 0).normalize();
//    private final Matrix4x3f directionMat = new Matrix4x3f();

    private final GameTick                   gameTick            = new GameTick();
    private final List<Consumer<BaseEntity>> onChangeSubscribers = new ArrayList<>();

    // Temp fields for calculations
    protected final Quaternionf tmpq           = new Quaternionf();
    protected final Vector3f    tmpMoveVectorX = new Vector3f();
    protected final Vector3f    tmpMoveVectorY = new Vector3f();
    protected final Vector3f    tmpMoveVectorZ = new Vector3f();
    private         long        lastGameTick;

    public BaseEntity(CommunicationManager communicationManager) {
        this.communicationManager = communicationManager;
    }


    /**
     * Закрепить точку position на другом Entity или произвольным объектом position
     *
     * @param baseEntity
     */
    public void linkPosition(BaseEntity baseEntity) {
        this.position = baseEntity.position;
    }

    public void linkPosition(Vector3f position) {
        this.position = position;
    }

    /**
     * Закрепить угол направления angle равным другому Entity или произвольным объектом angle
     *
     * @param baseEntity
     */
    public void linkAngle(BaseEntity baseEntity) {
        this.angle = baseEntity.angle;
    }

    public void linkAngle(Vector3f angle) {
        this.angle = angle;
    }

    public void addSubscriber(Consumer<BaseEntity> consumer) {
        onChangeSubscribers.add(consumer);
    }

    public void moveDelta(Vector3f delta) {
        if (beforeMove(delta)) {
            position.add(delta);
            afterMove();
            notifyOnChange();
        }
    }

    private boolean beforeMove(Vector3f delta) {
        if (collisionDetector != null) {
            collisionDetector.handleCollisions(hitbox, delta);
        }
//        return true;
        return delta.x != 0 || delta.y != 0 || delta.z != 0;
    }

    public void afterMove() {
        hitbox.setOffset(position);
    }

    protected void updateDirection() {
        tmpq.rotationX(0)
                .rotateY(angle.x)
                .positiveZ(directionH).negate(); // calc direction and store to dir
        directionH.normalize();
        tmpq.rotationX(angle.y) // см выше, в кватерионе ось X это Y...
                .rotateY(angle.x)
                //     .rotateLocalZ((float) Math.toRadians(roll)) // calc rotation and store to tmpq
                .positiveZ(direction).negate(); // calc direction and store to dir
        direction.normalize();

//        directionMat.rotation(tmpq);

    }

    public void rotateDelta(InputEventData inputEventData) {

        angle.x += inputEventData.f1;
        angle.y += inputEventData.f2;
        angle.z += inputEventData.f3;

        afterRotate();

        notifyOnChange();
    }

    private void notifyOnChange() {
        onChangeSubscribers.forEach(i -> i.accept(this));
    }

    protected void afterRotate() {
        updateDirection();
    }

  
    public void onChangeHandler(BaseEntity producer) {
        afterMove();
        afterRotate();
    }

    public void subscribeToOnChange(BaseEntity baseEntity) {
        baseEntity.addSubscriber(this::onChangeHandler);
    }

    public void goForward(InputEventData inputEventData) {
        isGoingForward = true;
        updateIsGoing();
    }

    public void goRight(InputEventData inputEventData) {
        isGoingRight = true;
        updateIsGoing();
    }

    public void goLeft(InputEventData inputEventData) {
        isGoingLeft = true;
        updateIsGoing();
    }

    public void goBack(InputEventData inputEventData) {
        isGoingBack = true;
        updateIsGoing();
    }

    public void goUp(InputEventData inputEventData) {
        isGoingUp = true;
        updateIsGoing();
    }

    public void goDown(InputEventData inputEventData) {
        isGoingDown = true;
        updateIsGoing();
    }

    public void stopForward(InputEventData inputEventData) {
        isGoingForward = false;
        updateIsGoing();
    }

    public void stopRight(InputEventData inputEventData) {
        isGoingRight = false;
        updateIsGoing();
    }

    public void stopLeft(InputEventData inputEventData) {
        isGoingLeft = false;
        updateIsGoing();
    }

    public void stopBack(InputEventData inputEventData) {
        isGoingBack = false;
        updateIsGoing();
    }

    public void stopUp(InputEventData inputEventData) {
        isGoingUp = false;
        updateIsGoing();
    }

    public void stopDown(InputEventData inputEventData) {
        isGoingDown = false;
        updateIsGoing();
    }

    public void updateIsGoing() {
        isGoing                    = isGoingForward || isGoingRight || isGoingLeft || isGoingBack || isGoingUp || isGoingDown || !isZeroVector(velocity);
        velocityGoingDirectionCoef = velocityGoingDirectionCoefMatrix[((isGoingForward || isGoingBack) ? 1 : 0) + ((isGoingRight || isGoingLeft) ? 1 : 0) + ((isGoingUp || isGoingDown) ? 1 : 0)];
    }

    public void updateVelocityVector() {

        if (isGoing) {
            velocity.x = smoothChangeVelocity(isGoingForward, isGoingBack, velocity.x);
            velocity.y = smoothChangeVelocity(isGoingUp, isGoingDown, velocity.y);
            velocity.z = smoothChangeVelocity(isGoingRight, isGoingLeft, velocity.z);

            if (isFly) {
                velocity.mul(flySpeed, speedVelocity);
            } else if (isSneak) {
                velocity.mul(sneakSpeed, speedVelocity);
            } else if (isSquat) {
                velocity.mul(squatSpeed, speedVelocity);
            } else if (isSwim) {
                velocity.mul(swimSpeed, speedVelocity);
            } else {
                velocity.mul(normalSpeed, speedVelocity);
            }
        }
    }

    private float smoothChangeVelocity(boolean plus, boolean minus, float vel) {
        float v = vel;
        if (plus && minus) {
            return 0;
        } else {
            if (plus) {
                return min(1.0f, v + FRACTION_START);
            } else if (minus) {
                return max(-1.0f, v - FRACTION_START);
            } else {
                v = v * FRACTION_STOP;
                if (v > -0.01f && v < 0.01f) {
                    v = 0;
                }
                return v;
            }
        }
    }

    public void applyGameTick(long gameTick) {
        if (isGoing) {
            updateVelocityVector();
            float dt = velocityGoingDirectionCoef * ((float) (gameTick - lastGameTick) * ONE_VELOCITY_PER_SECOND);

            // calc X delta (go forward)
            directionH.mul(dt * speedVelocity.x, tmpMoveVectorX);
            // calc Y delta (go up/down)
            UP_VECTOR.mul(dt * speedVelocity.y, tmpMoveVectorY);
            // calc Z delta (go left/right)
            directionH.cross(UP_VECTOR, tmpMoveVectorZ);
            tmpMoveVectorZ.mul(dt * speedVelocity.z);

            // calc finish vector
            tmpMoveVectorX.add(tmpMoveVectorZ).add(tmpMoveVectorY);

            moveDelta(tmpMoveVectorX);
//            if (tmpMoveVectorX.length() > 0.001) {
//                System.out.println("dt:" + dt + " tmpX:" + tmpMoveVectorX + " dirH:" + directionH);
//            }
            updateIsGoing();
        }
        lastGameTick = gameTick;

    }
}
