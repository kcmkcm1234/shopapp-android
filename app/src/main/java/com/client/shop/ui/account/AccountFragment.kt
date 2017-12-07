package com.client.shop.ui.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.client.shop.R
import com.client.shop.ShopApplication
import com.client.shop.const.RequestCode
import com.client.shop.ui.account.contract.AuthPresenter
import com.client.shop.ui.account.contract.AuthView
import com.client.shop.ui.account.di.AuthModule
import com.client.shop.ui.policy.PolicyActivity
import com.domain.entity.Policy
import com.domain.entity.Shop
import com.ui.base.lce.BaseFragment
import kotlinx.android.synthetic.main.fragment_account.*
import javax.inject.Inject

class AccountFragment : BaseFragment<Boolean, AuthView, AuthPresenter>(), AuthView {

    @Inject lateinit var authPresenter: AuthPresenter
    private var shop: Shop? = null

    companion object {

        private const val SHOP = "shop"

        fun newInstance(shop: Shop?): AccountFragment {
            val fragment = AccountFragment()
            val args = Bundle()
            args.putParcelable(SHOP, shop)
            fragment.arguments = args
            return fragment
        }
    }

    //ANDROID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        setupButtons()
        setupShop()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val compatActivity = activity
        if (compatActivity is AppCompatActivity) {
            toolbar.setTitle(getString(R.string.my_account))
            compatActivity.setSupportActionBar(toolbar)
            compatActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.SIGN_IN && resultCode == Activity.RESULT_OK) {
            //TODO UPDATE VIEW
        }
    }

    //INIT

    override fun inject() {
        ShopApplication.appComponent.attachAuthComponent(AuthModule()).inject(this)
    }

    override fun getContentView() = R.layout.fragment_account

    override fun createPresenter() = authPresenter

    //LCE

    override fun loadData(pullToRefresh: Boolean) {
        super.loadData(pullToRefresh)
        presenter.isAuthorized()
    }

    override fun showContent(data: Boolean) {
        super.showContent(data)
        if (data) {
        } else {
        }
    }

    override fun signedOut() {
        showMessage(R.string.logout_success_message)
        loadData()
    }

    //SETUP

    private fun setupButtons() {
        signInButton.setOnClickListener {
            startActivityForResult(SignInActivity.getStartIntent(context), RequestCode.SIGN_IN)
        }
        createAccount.setOnClickListener {
            startActivityForResult(SignUpActivity.getStartIntent(context, shop?.privacyPolicy, shop?.termsOfService), RequestCode.SIGN_UP)
        }
    }

    private fun setupShop() {
        shop = arguments.getParcelable(SHOP)
        shop?.let {
            setupPolicy(privacyPolicy, it.privacyPolicy)
            setupPolicy(refundPolicy, it.refundPolicy)
            setupPolicy(termsOfService, it.termsOfService)
        }
    }

    private fun setupPolicy(view: View, policy: Policy?) {
        if (policy != null) {
            view.setOnClickListener { startActivity(PolicyActivity.getStartIntent(context, policy)) }
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }
}