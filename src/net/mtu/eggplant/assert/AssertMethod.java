public class AssertMethod {

  public AssertMethod(AssertClass class, String name, Vector preConditions, Vector postConditions) {


  }

  /** contains the tokens that define the pre conditions **/  
  private Vector _preConditions;
  /** contains the tokens that define the post conditions **/
  private Vector _postConditions;

  /** x is the line, y is the column **/
  private Point _methodEntrance;
  
  /** Vector of Points **/
  private Vector _methodExits;

  /**
     @param params Vector of StringPairs, class, parameter name
  **/
  public void setParameters(Vector params) {


  }

  /**
     set the return type of this method, used for building post checks
  **/
  public void setReturnType(String retType) {

  }

}
