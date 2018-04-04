---
title: 仿微信侧滑删除的简单实现
grammar_cjkRuby: true
---

博客地址：https://blog.csdn.net/bjyanxin/article/details/79819856

首先看一下效果图：

![enter description here](./images/SwipeView.gif)

基本思路是自定义一个`ViewGroup`作为`RecyclerView`中的条目，通过手势侧滑拉出删除按钮，动态改变删除按钮的宽度。

在网上也看了很多类似的实现，大多非常麻烦，我参考了[Android动手实现一个侧滑删除控件，支持Recyclerview](https://www.jianshu.com/p/e2c92d5a3cdc)大神的实现，做出了这个控件。

# 实现过程

首先参考条目的布局：
```java?linenums
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="55dp"
    android:background="#0e0e0e">

    <xmt.baofeng.xmt.mobileapp.ui.view.SwipeView
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <LinearLayout
            android:id="@+id/ll_usb"
            android:layout_width="match_parent"
            android:layout_height="match_parent">

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_gravity="center_vertical"
                android:layout_marginLeft="15dp"
                android:layout_weight="1"
                android:text="湮灭.720p.HD中英双字"
                android:textColor="#999999"
                android:textSize="14sp"/>

        </LinearLayout>

        <TextView
            android:id="@+id/tv_usb_delete"
            android:layout_width="80dp"
            android:layout_height="match_parent"
            android:background="@color/colorRed"
            android:gravity="center"
            android:text="删除"
            android:textColor="#ffffff"
            android:textSize="14sp"/>
    </xmt.baofeng.xmt.mobileapp.ui.view.SwipeView>

</FrameLayout>
```

外层必须用`FrameLayout`或者其他布局包裹，内层是我们自定义的`SwipeView`，`SwipeView`内部只能有两个子`View`，第一个子`View`是我们需要显示的内容，第二个子`View`就是删除按钮。

剩下的重头戏就在自定义`SwipeView`了，`SwipeView`继承自`ViewGroup`，下面两个方法没什么好说，就是测量和布局。

```java?linenums
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        View child = getChildAt(0);
        int margin = ((MarginLayoutParams) child.getLayoutParams()).topMargin
                + ((MarginLayoutParams) child.getLayoutParams()).bottomMargin;
        setMeasuredDimension(width, getChildAt(0).getMeasuredHeight() + margin);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        getChildAt(0).layout(l, t, r, b);

        View view = getChildAt(1);
        view.layout(r, t, r + view.getMeasuredWidth(), b);	//删除按钮排布在第一个View之后
    }
```

接下来是最重要的`onTouchEvent`的实现：

```java?linenums
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!scroller.isFinished()){
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                moveX = (int) event.getRawX();
                int moved = moveX - downX;
                if (isShowRight) {
                    moved -= deleteWidth;	//如果已经展示出删除按钮，移动距离+删除按钮的宽度
                }
                if (-moved <= deleteWidth) {
                    scrollTo(-moved, 0);
                } else {
                    int delta = -moved - deleteWidth;
                    int percent = (int) ((deleteWidth - delta / 5) / (float) deleteWidth * delta);	//这里做了一个限制，越往左划越难划，没什么卵用，就是看上去酷一点
                    View view = getChildAt(1);
                    LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.width = deleteWidth + percent;	//随着向左划增加删除按钮的宽度
                    view.setLayoutParams(layoutParams);
                    scrollTo(deleteWidth + percent, 0);
                }
                if (getScrollX() <= 0) {
                    scrollTo(0, 0);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (getScrollX() >= deleteWidth / 2) {
                    isShowRight = true;
                    sView = this;
                    recoverAnimation();
                    smoothScrollToPosition(deleteWidth);
                } else {
                    isShowRight = false;
                    smoothScrollToPosition(0);
                }
                break;
            default:
                break;
        }
        return true;
    }
```
```java?linenums
	private void smoothScrollToPosition(int destX) {	//利用scroller实现弹性滑动
        int width = getScrollX();
        int delta = destX - width;
        scroller.startScroll(width, 0, delta, 0, 500);
        invalidate();
    }
	
	private void recoverAnimation() {	//在手抬起时需要恢复删除按钮的宽度
        final View view = getChildAt(1);
        final SwipeLayoutParams swipeLayoutParams = new SwipeLayoutParams(view.getLayoutParams());
        ObjectAnimator animator = ObjectAnimator.ofInt(swipeLayoutParams, "width", deleteWidth).setDuration(500);
        animator.setInterpolator(new ViscousFluidInterpolator());	//使用和scroller一样的插值器，我直接把scroller的插值器粘贴过来了=。=
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setLayoutParams(swipeLayoutParams);
            }
        });
        animator.start();
    }
```

deleteWidth是我们在按下时获取到的删除按钮的宽度，因为一会儿会改变按钮的宽度，所以在DOWN时记录原始宽度，DOWN在后面onInterceptTouchEvent方法中。

```java?linenums
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) ev.getRawX();
                if (sView != null && sView != this) {
                    sView.close();
                }
                deleteWidth = getChildAt(1).getWidth();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = (int) ev.getRawX();
                if (Math.abs(moveX - downX) > MAX_BOUNDARY) {
                    getParent().requestDisallowInterceptTouchEvent(true);	//在左划时拦截事件，防止左划同时还能上下滑动
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
```

感觉有很多不合理的地方，希望有大神指出，阿里嘎多~~~