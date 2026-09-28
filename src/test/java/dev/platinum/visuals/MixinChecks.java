package dev.platinum.visuals;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

/** Audits exact injection selectors against the build's Minecraft bytecode without starting a game. */
public final class MixinChecks {
  private static int targets,invokes;
  private static ClassNode read(String name)throws Exception{
    try(var input=MixinChecks.class.getClassLoader().getResourceAsStream(name+".class")){
      if(input==null)throw new AssertionError("Missing class: "+name);
      ClassNode node=new ClassNode();new ClassReader(input).accept(node,0);return node;
    }
  }
  private static List<AnnotationNode> annotations(List<AnnotationNode> visible,List<AnnotationNode> invisible){List<AnnotationNode> result=new ArrayList<>();if(visible!=null)result.addAll(visible);if(invisible!=null)result.addAll(invisible);return result;}
  private static Object value(AnnotationNode a,String key){if(a.values!=null)for(int i=0;i<a.values.size();i+=2)if(a.values.get(i).equals(key))return a.values.get(i+1);return null;}
  private static List<?> list(Object v){return v==null?List.of():v instanceof List<?> values?values:List.of(v);}
  private static void require(boolean value,String detail){if(!value)throw new AssertionError(detail);}
  public static void main(String[] args)throws Exception{
    try(var stream=MixinChecks.class.getClassLoader().getResourceAsStream("platinumvisuals.mixins.json")){
      require(stream!=null,"Mixin manifest packaged");var manifest=JsonParser.parseReader(new InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
      String prefix=manifest.get("package").getAsString().replace('.','/')+"/";
      for(var entry:manifest.getAsJsonArray("client")){
        ClassNode mixin=read(prefix+entry.getAsString());List<ClassNode> targetClasses=new ArrayList<>();
        for(var a:annotations(mixin.visibleAnnotations,mixin.invisibleAnnotations))if(a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;"))for(Object t:list(value(a,"value")))targetClasses.add(read(((Type)t).getInternalName()));
        require(!targetClasses.isEmpty(),"Mixin must target class: "+mixin.name);
        for(MethodNode handler:mixin.methods)for(var annotation:annotations(handler.visibleAnnotations,handler.invisibleAnnotations)){
          if(!annotation.desc.endsWith("/Inject;")&&!annotation.desc.endsWith("/ModifyExpressionValue;")&&!annotation.desc.endsWith("/ModifyVariable;"))continue;
          for(Object item:list(value(annotation,"method"))){String selector=(String)item;int split=selector.indexOf('(');String name=split<0?selector:selector.substring(0,split),desc=split<0?null:selector.substring(split);
            for(ClassNode target:targetClasses){List<MethodNode> matches=target.methods.stream().filter(m->m.name.equals(name)&&(desc==null||m.desc.equals(desc))).toList();require(!matches.isEmpty(),mixin.name+": missing "+target.name+"."+selector);targets++;
              if(annotation.desc.endsWith("/Inject;"))for(MethodNode method:matches){Type[] signature=Type.getArgumentTypes(handler.desc),expected=Type.getArgumentTypes(method.desc);boolean callbackOnly=signature.length==1;if(!callbackOnly){require(signature.length==expected.length+1,"Handler argument count: "+handler.name);for(int i=0;i<expected.length;i++)require(signature[i].equals(expected[i]),"Handler argument type: "+handler.name+" arg "+i);}}
              if(annotation.desc.endsWith("/ModifyVariable;")){
                Type[] handlerArgs=Type.getArgumentTypes(handler.desc);require(handlerArgs.length==1&&handlerArgs[0].equals(Type.getReturnType(handler.desc)),"Variable modifier signature: "+handler.name);
                require(Boolean.TRUE.equals(value(annotation,"argsOnly")),"Audit currently supports argument-only modifiers");
                int ordinal=((Number)value(annotation,"ordinal")).intValue();
                for(MethodNode method:matches){long count=java.util.Arrays.stream(Type.getArgumentTypes(method.desc)).filter(t->t.equals(handlerArgs[0])).count();require(ordinal>=0&&ordinal<count,"Modifier argument ordinal: "+handler.name);}
              }
              for(Object atValue:list(value(annotation,"at"))){AnnotationNode at=(AnnotationNode)atValue;if(!"INVOKE".equals(value(at,"value")))continue;String invoke=(String)value(at,"target");int semi=invoke.indexOf(';'),paren=invoke.indexOf('(',semi);String owner=invoke.substring(1,semi),invName=invoke.substring(semi+1,paren),invDesc=invoke.substring(paren);boolean found=false;for(MethodNode method:matches)for(AbstractInsnNode instruction:method.instructions)if(instruction instanceof MethodInsnNode call&&call.owner.equals(owner)&&call.name.equals(invName)&&call.desc.equals(invDesc))found=true;require(found,mixin.name+": invocation missing "+invoke);invokes++;}
            }
          }
        }
      }
    }
    System.out.println("MixinChecks: "+targets+" method selectors and "+invokes+" invocation selectors verified (static audit only)");
  }
}
