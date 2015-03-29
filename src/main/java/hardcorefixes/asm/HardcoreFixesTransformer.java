package hardcorefixes.asm;

import hardcorefixes.HardcoreFixes;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;

public class HardcoreFixesTransformer implements IClassTransformer, Opcodes
{
    private enum TransformType
    {
        METHOD, FIELD, INNER_CLASS, MODIFY, MAKE_PUBLIC, DELETE
    }

    private enum Transformer
    {
        ITEM_SEARCH("textChanged", "(Lhardcorequesting/client/interfaces/GuiBase;)V")
                {
                    @Override
                    protected InsnList modifyInstructions(InsnList list)
                    {
                        AbstractInsnNode first = list.getFirst();
                        list.insertBefore(first, new VarInsnNode(ALOAD, 0));
                        list.insertBefore(first, new FieldInsnNode(GETFIELD, "hardcorequesting/client/interfaces/GuiEditMenuItem$2", "this$0", "Lhardcorequesting/client/interfaces/GuiEditMenuItem;"));
                        list.insertBefore(first, new VarInsnNode(ALOAD, 0));
                        list.insertBefore(first, new MethodInsnNode(INVOKEVIRTUAL, "hardcorequesting/client/interfaces/GuiEditMenuItem$2","getText", "()Ljava/lang/String;", false));
                        list.insertBefore(first, new MethodInsnNode(INVOKESTATIC, "hardcorefixes/asm/FixesHooks", "updateSearch", "(Lhardcorequesting/client/interfaces/GuiEditMenuItem;Ljava/lang/String;)V", false));
                        list.insertBefore(first, new InsnNode(RETURN));
                        return list;
                    }
                },
        INIT_FLUID("<INIT>", "(Lnet/minecraftforge/fluids/Fluid;)V", TransformType.METHOD, TransformType.MAKE_PUBLIC),
        INIT_ITEM("<INIT>", "(Lnet/minecraft/item/ItemStack;)V", TransformType.METHOD, TransformType.MAKE_PUBLIC),
        PUBLIC_TYPE("type", "Lhardcorequesting/client/interfaces/GuiEditMenuItem$Type;", TransformType.FIELD, TransformType.MAKE_PUBLIC),
        PUBLIC_FLUID("allowFluids", "Z", TransformType.FIELD, TransformType.MAKE_PUBLIC),
        PUBLIC_SEARCH("searchItems", "Ljava/util/List;", TransformType.FIELD, TransformType.MAKE_PUBLIC),
        GET_FLUID_ELEMENT("getFluidElement", "(Lnet/minecraftforge/fluids/Fluid;)Lhardcorequesting/client/interfaces/GuiEditMenuItem$ElementFluid;")
                {
                    @Override
                    protected InsnList modifyInstructions(InsnList list)
                    {
                        InsnList result = new InsnList();
                        result.add(new TypeInsnNode(NEW, "hardcorequesting/client/interfaces/GuiEditMenuItem$ElementFluid"));
                        result.add(new InsnNode(DUP));
                        result.add(new VarInsnNode(ALOAD, 0));
                        result.add(new MethodInsnNode(INVOKESPECIAL, "hardcorequesting/client/interfaces/GuiEditMenuItem$ElementFluid", "<init>", "(Lnet/minecraftforge/fluids/Fluid;)V",false));
                        result.add(new InsnNode(ARETURN));
                        return result;
                    }
                },
        GET_ITEM_ELEMENT("getItemElement", "(Lnet/minecraft/item/ItemStack;)Lhardcorequesting/client/interfaces/GuiEditMenuItem$ElementItem;")
                {
                    @Override
                    protected InsnList modifyInstructions(InsnList list)
                    {
                        InsnList result = new InsnList();
                        result.add(new TypeInsnNode(NEW, "hardcorequesting/client/interfaces/GuiEditMenuItem$ElementItem"));
                        result.add(new InsnNode(DUP));
                        result.add(new VarInsnNode(ALOAD, 0));
                        result.add(new MethodInsnNode(INVOKESPECIAL, "hardcorequesting/client/interfaces/GuiEditMenuItem$ElementItem", "<init>", "(Lnet/minecraft/item/ItemStack;)V",false));
                        result.add(new InsnNode(ARETURN));
                        return result;
                    }
                },
        GET_SEARCH_ITEMS("getSearchItems", "(Lhardcorequesting/client/interfaces/GuiEditMenuItem;)Ljava/util/List;")
                {
                    @Override
                    protected InsnList modifyInstructions(InsnList list)
                    {
                        InsnList result = new InsnList();
                        result.add(new VarInsnNode(ALOAD, 0));
                        result.add(new FieldInsnNode(GETFIELD, "hardcorequesting/client/interfaces/GuiEditMenuItem", "searchItems", "Ljava/util/List;"));
                        result.add(new InsnNode(ARETURN));
                        return result;
                    }
                },
        SHOW_FLUIDS("showFluid", "(Lhardcorequesting/client/interfaces/GuiEditMenuItem;)Z")
                {
                    @Override
                    protected InsnList modifyInstructions(InsnList list)
                    {
                        InsnList result = new InsnList();
                        result.add(new VarInsnNode(ALOAD, 0));
                        result.add(new FieldInsnNode(GETFIELD, "hardcorequesting/client/interfaces/GuiEditMenuItem", "type", "Lhardcorequesting/client/interfaces/GuiEditMenuItem$Type;"));
                        result.add(new FieldInsnNode(GETFIELD, "hardcorequesting/client/interfaces/GuiEditMenuItem$Type", "allowFluids", "Z"));
                        result.add(new InsnNode(IRETURN));
                        return result;
                    }
                };

        private String name;
        private String args;
        private TransformType type;
        private TransformType action;

        Transformer(String name)
        {
            this(name, "", TransformType.INNER_CLASS, TransformType.MAKE_PUBLIC);
        }

        Transformer(String name, String args)
        {
            this(name, args, TransformType.METHOD, TransformType.MODIFY);
        }

        Transformer(String name, String args, TransformType type, TransformType action)
        {
            this.name = name;
            this.args = args;
            this.type = type;
            this.action = action;
        }

        protected InsnList modifyInstructions(InsnList list)
        {
            return list;
        }

        private static InsnList replace(InsnList list, String toReplace, String replace)
        {
            AbstractInsnNode node = list.getFirst();
            InsnList result = new InsnList();
            while (node != null)
            {
                result.add(checkReplace(node, toReplace, replace));
                node = node.getNext();
            }
            return result;
        }

        public String getName()
        {
            return name;
        }

        public String getArgs()
        {
            return args;
        }

        private void methodTransform(ClassNode node)
        {
            MethodNode methodNode = getMethod(node);
            if (methodNode != null)
            {
                switch (action)
                {
                    case MODIFY:
                        methodNode.instructions = modifyInstructions(methodNode.instructions);
                        break;
                    case DELETE:
                        node.methods.remove(methodNode);
                        break;
                    case MAKE_PUBLIC:
                        methodNode.access = (methodNode.access & ~7) ^ 1;
                }
                complete();
            }
        }

        private void fieldTransform(ClassNode node)
        {
            FieldNode fieldNode = getField(node);
            if (fieldNode != null)
            {
                switch (action)
                {
                    case MODIFY:
                        modifyField(fieldNode);
                        break;
                    case DELETE:
                        node.fields.remove(fieldNode);
                        break;
                    case MAKE_PUBLIC:
                        fieldNode.access = (fieldNode.access & ~7) ^ 1;
                }
                complete();
            }
        }

        private void modifyField(FieldNode fieldNode)
        {
        }


        private void innerClassTransform(ClassNode node)
        {
            InnerClassNode innerClassNode = getInnerClass(node);
            if (innerClassNode != null)
            {
                switch (action)
                {
                    case MODIFY:
                        modifyInnerClass(innerClassNode);
                        break;
                    case DELETE:
                        node.innerClasses.remove(innerClassNode);
                        break;
                    case MAKE_PUBLIC:
                        innerClassNode.access = (innerClassNode.access & ~7) ^ 1;
                }
                complete();
            }
        }

        private void modifyInnerClass(InnerClassNode innerClassNode)
        {
        }

        public void transform(ClassNode node)
        {
            switch (this.type)
            {
                case METHOD:
                    methodTransform(node);
                    return;
                case FIELD:
                    fieldTransform(node);
                    return;
                case INNER_CLASS:
                    innerClassTransform(node);
            }
        }

        private static AbstractInsnNode checkReplace(AbstractInsnNode node, String toReplace, String replace)
        {
            if (node instanceof TypeInsnNode && ((TypeInsnNode)node).desc.equals(toReplace))
            {
                return new TypeInsnNode(NEW, replace);
            } else if (node instanceof MethodInsnNode && ((MethodInsnNode)node).owner.contains(toReplace))
            {
                return new MethodInsnNode(node.getOpcode(), replace, ((MethodInsnNode)node).name, ((MethodInsnNode)node).desc, false);
            }
            return node;
        }

        public void complete()
        {
            HardcoreFixes.log.info("Applied " + this + " transformer");
        }

        public MethodNode getMethod(ClassNode classNode)
        {
            for (MethodNode method : classNode.methods)
            {
                if (method.name.equals(getName()) && method.desc.equals(getArgs()))
                {
                    return method;
                }
            }
            for (MethodNode method : classNode.methods)
            {
                if (method.desc.equals(getArgs()))
                {
                    return method;
                }
            }
            return null;
        }

        public FieldNode getField(ClassNode classNode)
        {
            for (FieldNode field : classNode.fields)
            {
                if (field.name.equals(getName()) && field.desc.equals(getArgs()))
                {
                    return field;
                }
            }
            return null;
        }

        public InnerClassNode getInnerClass(ClassNode classNode)
        {
            String name = classNode.name + "$" + getName();
            for (InnerClassNode inner : classNode.innerClasses)
            {
                if (name.equals(inner.name))
                {
                    return inner;
                }
            }
            return null;
        }
    }

    private enum ClassName
    {
        SEARCH("hardcorequesting.client.interfaces.GuiEditMenuItem$2", Transformer.ITEM_SEARCH),
        MENU_ITEM("hardcorequesting.client.interfaces.GuiEditMenuItem", Transformer.PUBLIC_TYPE, Transformer.PUBLIC_SEARCH),
        TYPE("hardcorequesting.client.interfaces.GuiEditMenuItem$Type", Transformer.PUBLIC_FLUID),
        FLUID("hardcorequesting.client.interfaces.GuiEditMenuItem$ElementFluid", Transformer.INIT_FLUID),
        ITEM("hardcorequesting.client.interfaces.GuiEditMenuItem$ElementItem", Transformer.INIT_ITEM),
        SEARCH_ITEMS("hardcorefixes.threading.Search", Transformer.GET_FLUID_ELEMENT, Transformer.GET_ITEM_ELEMENT, Transformer.SHOW_FLUIDS),
        THREADING_HANDLER("hardcorefixes.handlers.ThreadingHandler", Transformer.GET_SEARCH_ITEMS);

        private String name;
        private Transformer[] transformers;

        ClassName(String name, Transformer... transformers)
        {
            this.name = name;
            this.transformers = transformers;
        }

        public String getName()
        {
            return name;
        }

        public Transformer[] getTransformers()
        {
            return transformers;
        }

        public byte[] transform(byte[] bytes)
        {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);

            HardcoreFixes.log.log(Level.INFO, "Applying Transformer" + (transformers.length > 1 ? "s " : " ") + "to " + getName());

            for (Transformer transformer : getTransformers())
            {
                transformer.transform(classNode);
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }
    }

    private static Map<String, ClassName> classMap = new HashMap<String, ClassName>();

    static
    {
        for (ClassName className : ClassName.values()) classMap.put(className.getName(), className);
    }

    @Override
    public byte[] transform(String className, String className2, byte[] bytes)
    {
        ClassName clazz = classMap.get(className);
        if (clazz != null)
        {
            bytes = clazz.transform(bytes);
            classMap.remove(className);
        }
        return bytes;
    }
}
