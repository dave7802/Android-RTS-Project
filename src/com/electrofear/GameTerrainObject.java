package com.electrofear;

import java.util.ArrayList;

public class GameTerrainObject extends GameObject {
    private float zAxis;
    private float height, width;
    private float angle, defaultAngle;
    private float turnRate; //angle per second
    private float relativeX, relativeY; //relative to parent
    private float calculatedX, calculatedY;
    private float shadowMultiplier;
    private boolean angleRelativeToParent;
    private DrawableObject mDrawable; //actual image or so
    private DrawableObject mDrawableShadow;
    private DrawableObject mDrawableLight;
    private Texture mTexture;
    private GameTerrainObject mParent = null;
    private ArrayList<GameTerrainObject> mChildTerrainObjList;
    
    private String gameUnitObjectID;
    private String internalId, relativeId;
    
    public GameTerrainObject(String objId,
    				      String inputInternalId,
    				      String inputRelativeId,
                          float width2, 
                          float height2, 
                          float relativeX, 
                          float relativeY,
                          float inputAngle,
                          float inputZAxis,
                          boolean angleRelativeToParent,
                          DrawableObject inputDrawable,
                          DrawableObject inputDrawableShadow,
                          DrawableObject inputDrawableLight){
        gameUnitObjectID = objId;
        internalId = inputInternalId;
        relativeId = inputRelativeId;
        mChildTerrainObjList = new ArrayList();
        mParent = null;
        height = height2;
        width = width2;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.calculatedX = 0;
        this.calculatedY = 0;
        angle = inputAngle;
        defaultAngle = inputAngle; //APPLIES TO TURRET ONLY IN THIS CASE
        mDrawable = inputDrawable;
        zAxis = inputZAxis;
        this.angleRelativeToParent = angleRelativeToParent;
        shadowMultiplier = 1;
        //Shadow Drawables
        mDrawableShadow = inputDrawableShadow;
        //Light Drawables
        mDrawableLight = inputDrawableLight;
    }
    
    public void setShadowMultiplier(float inputMultiplier) {
    	shadowMultiplier = inputMultiplier;
    }
    
    public void setAngle(float inputAngle){
    	this.angle = inputAngle;
    }

    public float getAngle(){
    	return this.angle;
    }
    
    public float getDefaultAngle(){
    	return this.defaultAngle;
    }
    
    //ADDS OJBECT BY INTERNAL ID, ALSO CHANGES THE Z-AXIS with Z-AXIS+1
    public void addGameUnitObjectByInternalId(GameTerrainObject obj){
    	
    	if (this.internalId.equals(obj.relativeId)) {
    		obj.mParent = this;
    		//obj.zAxis = this.zAxis + 1.0f;
    		this.mChildTerrainObjList.add(obj);
    		return;
    	} else {
    		for (int i = 0; i < mChildTerrainObjList.size(); i++) {
    			mChildTerrainObjList.get(i).addGameUnitObjectByInternalId(obj);
    		}
    	}
    }    
    
    //ADDS OBJECT That DOES NOT NEED INTERNAL/EXTERNAL ID Relationship
    public void addGameUnit(GameTerrainObject obj) {
    	obj.mParent = this;
    	//obj.zAxis = this.zAxis + 1.0f;
    	this.mChildTerrainObjList.add(obj);
    }
    
    public void setRelativeTranslateXY(float inputX, float inputY){
        relativeX = inputX;
        relativeY = inputY;
    }
    
    public void setCalculatedTranslateXY(float x, float y) {
        // TODO Auto-generated method stub
        calculatedX = x;
        calculatedY = y;
    }
    
    public void setHeightWidth(float width, float height){
        this.height = height;
        this.width = width;
    }
    
    @Override
	public Vector2 getPosition(){
    	return new Vector2(calculatedX, calculatedY);
    }
    
    public void calculate(){
        //get current relative X, Y and translate
        //get angle and rotate
        //translate by parent X,Y
        if (this.mParent != null){
            String testing = this.gameUnitObjectID;
            float calculatingX = this.relativeX;
            float calculatingY = this.relativeY;
            float currentAngle = this.angle;
            
            //For TURRETS and OTHERS, DEFAULT ANGLE WILL ALWAYS BE relative to parent; (SHARE THE PARENT ANGLE)
            this.defaultAngle = this.mParent.getAngle();
         
            //do rotation calculation, if true base angle off parent, else use your own
            if (angleRelativeToParent){
                this.angle = this.mParent.angle;
            }
            double radians = Math.toRadians(this.mParent.angle % 360);
            //double unitLength = Math.sqrt(calculatingX * calculatingX + calculatingY * calculatingY);
            
            calculatingX = (float) (Math.cos(radians) * (relativeX) - Math.sin(radians) * (relativeY));
            calculatingY = (float) (Math.sin(radians) * (relativeX) + Math.cos(radians) * (relativeY));

            
            //calculatingY =  (float) (Math.sin(radians) * unitLength) * this.relativeY;
            //calculatingX =  (float) (Math.cos(radians) * unitLength) * this.relativeX;
           
            calculatingX += this.mParent.calculatedX;
            calculatingY += this.mParent.calculatedY;
            
            //NEW CENTER OF TURRET
            this.calculatedX = calculatingX;
            this.calculatedY = calculatingY;
            
            this.zAxis = mParent.zAxis + 1.0f;
        } else {
        	zAxis = this.zAxis;
        }
            
        //DrawableObject and then update
        mDrawable.updateCoords(this.calculatedX, this.calculatedY, width, height, angle, zAxis);
        
        if (mDrawableShadow != null) {
        	this.calculateShadowPositions(this.calculatedX, this.calculatedY, width, height, angle, zAxis);
        }
        //DrawableObject for light
    	if (mDrawableLight != null) {
    		mDrawableLight.updateCoords(this.calculatedX, this.calculatedY, width, height, angle, zAxis + 0.1f);
    	}
        
        
        for (int i = 0; i < this.mChildTerrainObjList.size(); i++){
            this.mChildTerrainObjList.get(i).calculate();
        }        
    }
    
    private void calculateShadowPositions(float x, float y, float width, float height, float angle, float zAxis){
    	float shadowX,shadowY;
    	float unitLength;
    	
    	unitLength = (float) Math.sqrt( BaseObject.directionShadow.x * BaseObject.directionShadow.x + BaseObject.directionShadow.y * BaseObject.directionShadow.y);
    	
    	shadowX = x + (BaseObject.directionShadow.x / unitLength) * BaseObject.lightSystem.currentShadowHeight * shadowMultiplier;
    	shadowY = y + (BaseObject.directionShadow.y / unitLength) * BaseObject.lightSystem.currentShadowHeight * shadowMultiplier;
    	if (mDrawableShadow != null) {
    		mDrawableShadow.updateCoords(shadowX, shadowY, width, height, angle, zAxis - mDrawableShadow.getShadowDifference());
    	}
    }
    
    public void addGameUnitObject(GameTerrainObject obj){
        obj.mParent = this;
        this.mChildTerrainObjList.add(obj);
    }
    
    @Override
	public void update(float timeDelta, BaseObject parent){
    	GameObject unitParent = (GameObject)parent;
    	Vector2 parentPosition = unitParent.getPosition();
    	this.setCalculatedTranslateXY(parentPosition.x, parentPosition.y);
    	
        //calculate everything
        this.calculate();
        this.scheduleDrawables();
    }
    
    //Schedulecurrentdrawable and then go down list and then create all child drawables
    public void scheduleDrawables(){
        if (mDrawable != null){
        	
            //DRAW SHADOW, draw shadow first before actual object!
            if (BaseObject.drawShadow) {
            	if (mDrawableShadow != null) {
            		BaseObject.renderSystem.scheduleForDraw(mDrawableShadow);
            	}
            }
            
            if (mDrawableLight != null) {
            	BaseObject.renderSystem.scheduleForDraw(mDrawableLight);
            }
        	
            BaseObject.renderSystem.scheduleForDraw(mDrawable);
            

            
        }
        for (int i = 0; i < this.mChildTerrainObjList.size(); i++){
            this.mChildTerrainObjList.get(i).scheduleDrawables();
        }        
    }



}
